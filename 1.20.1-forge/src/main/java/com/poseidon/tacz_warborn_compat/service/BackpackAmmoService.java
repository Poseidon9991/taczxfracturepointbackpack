package com.poseidon.tacz_warborn_compat.service;

import com.poseidon.tacz_warborn_compat.TaczWarbornCompat;
import com.poseidon.tacz_warborn_compat.config.WarbornCompatConfig;
import com.raiiiden.warborn.common.item.BackpackItem;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

// Bridge between TaCZ's ammo flow and WARBORN backpack storage.
//
// Query path (canReload / hasInventoryAmmo / hasAmmoToConsume): our mixins
// inject at RETURN and flip a "false" result to "true" when a backpack holds
// compatible ammo. This composes with other compat mods, unlike @Redirect on
// the capability lookup which is exclusive.
//
// Consume path (consumeAmmoFromPlayer): TaCZ drains the normal inventory first;
// our mixin then extracts the remaining amount from backpacks and bumps the
// returned count so reload math stays correct.
//
// consumeFromBackpacks mirrors the handler into the stack's BackpackCap
// tag after an extraction so the stack NBT matches the handler.
public final class BackpackAmmoService {

    private BackpackAmmoService() {
    }

    // Mirrors the slot scan inside TaCZ's canReload / hasAmmoToConsume lambdas:
    // any IAmmo stack matching the gun, or any IAmmoBox matching the gun.
    //
    // Read-only and safe on the client — LocalPlayerReload gates the reload
    // packet on a client-side canReload check, so the scan must work there or
    // reload never starts.
    public static boolean hasBackpackAmmo(LivingEntity entity, ItemStack gunStack) {
        if(!(entity instanceof Player player) || !WarbornCompatConfig.isEnabled()) {
            return false;
        }
        // Dummy-ammo guns feed from an internal pool and never touch item
        // ammo. OR-ing the backpack scan would make hasAmmoToConsume lie while
        // consumeAmmoFromPlayer stays on the dummy path, letting per-round
        // reload scripts load rounds for free.
        if(gunStack.getItem() instanceof IGun igun && igun.useDummyAmmo(gunStack)) {
            return false;
        }
        for(IItemHandler storage : collectStorage(player)) {
            for(int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if(stack.isEmpty()) {
                    continue;
                }
                // Simulated extract mirrors what the consume path can really
                // take: getStackInSlot alone reports ammo in slots a handler
                // refuses to give back, which would lie the same way.
                if(stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)
                        && !storage.extractItem(i, 1, true).isEmpty()) {
                    return true;
                }
                if(stack.getItem() instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(gunStack, stack)
                        && ammoBox.getAmmoCount(stack) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // Mirrors GunHudOverlay.handleInventoryAmmo so the HUD "reserve" number
    // also includes backpack contents: IAmmo stacks add their count, matching
    // IAmmoBox adds its stored count, and a creative box forces 9999.
    public static int countBackpackAmmo(LivingEntity entity, ItemStack gunStack, int baseCount) {
        if(!(entity instanceof Player player) || !WarbornCompatConfig.isEnabled()) {
            return baseCount;
        }
        int total = baseCount;
        for(IItemHandler storage : collectStorage(player)) {
            for(int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if(stack.isEmpty()) {
                    continue;
                }
                if(stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
                    total += stack.getCount();
                } else if(stack.getItem() instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(gunStack, stack)) {
                    if(ammoBox.isAllTypeCreative(stack) || ammoBox.isCreative(stack)) {
                        return 9999;
                    }
                    total += ammoBox.getAmmoCount(stack);
                }
            }
        }
        return total;
    }

    // Guns that never extract real items (dummy/virtual ammo, or inventory-feed
    // guns when the shooter skips ammo checks) must not touch backpack storage.
    public static boolean usesVirtualAmmo(AbstractGunItem gun, ItemStack gunStack, LivingEntity shooter) {
        IGunOperator operator = IGunOperator.fromLivingEntity(shooter);
        boolean needsAmmoCheck = operator != null && operator.needCheckAmmo();
        return (gun.useInventoryAmmo(gunStack) && !needsAmmoCheck) || gun.useDummyAmmo(gunStack);
    }

    @SuppressWarnings("unchecked")
    public static int consumeFromBackpacks(Player player, AbstractGunItem gun, ItemStack gunStack, int remaining) {
        int consumed = 0;
        List<ItemStack> backpacks = collectBackpackStacks(player);
        for(ItemStack bpStack : backpacks) {
            if(remaining <= 0) {
                break;
            }
            var cap = bpStack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if(cap.isEmpty()) {
                continue;
            }
            IItemHandler storage = cap.get();
            int extracted = gun.findAndExtractInventoryAmmo(storage, gunStack, remaining);
            if(extracted > 0) {
                consumed += extracted;
                remaining -= extracted;
                if(storage instanceof INBTSerializable<?> serializable) {
                    bpStack.getOrCreateTag().put("BackpackCap", ((INBTSerializable<CompoundTag>) serializable).serializeNBT());
                }
            }
        }
        return consumed;
    }

    private static long lastDebugLog;

    private static List<IItemHandler> collectStorage(Player player) {
        List<IItemHandler> out = new ArrayList<>();
        for(ItemStack bpStack : collectBackpackStacks(player)) {
            addIfBackpack(out, bpStack);
        }
        logStorages(player, out);
        return out;
    }

    private static void logStorages(Player player, List<IItemHandler> storages) {
        if(!WarbornCompatConfig.debugEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        if(now - lastDebugLog <= 3000) {
            return;
        }
        lastDebugLog = now;
        StringBuilder contents = new StringBuilder();
        for(IItemHandler storage : storages) {
            int nonEmpty = 0;
            for(int i = 0; i < storage.getSlots(); i++) {
                if(!storage.getStackInSlot(i).isEmpty()) {
                    nonEmpty++;
                }
            }
            if(contents.length() > 0) {
                contents.append(',');
            }
            contents.append(nonEmpty).append('/').append(storage.getSlots());
        }
        TaczWarbornCompat.LOGGER.info("[TaCZ-WB] {} -> {} storages [{} filled/slots]",
                player.level().isClientSide ? "client" : "server", storages.size(), contents);
    }

    // Every WARBORN backpack stack the player can feed from: chest armor
    // slot, any Curios slot (soft dep, guarded), and the inventory.
    public static List<ItemStack> collectBackpackStacks(Player player) {
        List<ItemStack> out = new ArrayList<>();

        if(WarbornCompatConfig.wornEnabled()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if(BackpackItem.isBackpackItem(chest) && !out.contains(chest)) {
                out.add(chest);
            }
            if(ModList.get().isLoaded("curios")) {
                List<ItemStack> curioStacks = new ArrayList<>();
                CuriosCompat.collectStacks(player, curioStacks);
                for(ItemStack stack : curioStacks) {
                    if(BackpackItem.isBackpackItem(stack) && !out.contains(stack)) {
                        out.add(stack);
                    }
                }
            }
        }

        if(WarbornCompatConfig.inventoryEnabled()) {
            Inventory inventory = player.getInventory();
            for(int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if(BackpackItem.isBackpackItem(stack) && !out.contains(stack)) {
                    out.add(stack);
                }
            }
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    private static void addIfBackpack(List<IItemHandler> out, ItemStack stack) {
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler instanceof INBTSerializable<?> serializable && stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if(tag != null && tag.contains("BackpackCap", Tag.TAG_COMPOUND)) {
                    boolean handlerEmpty = true;
                    for(int i = 0; i < handler.getSlots(); i++) {
                        if(!handler.getStackInSlot(i).isEmpty()) {
                            handlerEmpty = false;
                            break;
                        }
                    }
                    if(handlerEmpty) {
                        CompoundTag capTag = tag.getCompound("BackpackCap");
                        if(capTag.contains("Items", Tag.TAG_LIST) && !capTag.getList("Items", Tag.TAG_COMPOUND).isEmpty()) {
                            ((INBTSerializable<CompoundTag>) serializable).deserializeNBT(capTag);
                        }
                    }
                }
            }
            out.add(handler);
        });
    }
}
