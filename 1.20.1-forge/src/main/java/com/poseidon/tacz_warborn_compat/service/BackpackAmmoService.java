package com.poseidon.tacz_warborn_compat.service;

import com.poseidon.tacz_warborn_compat.config.WarbornCompatConfig;
import com.raiiiden.warborn.common.item.BackpackItem;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
// WARBORN backpacks expose ForgeCapabilities.ITEM_HANDLER on the ItemStack
// itself; handler edits are the live state and persist through Forge's normal
// capability save cycle, so no manual write-back is needed after extraction.
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
        for(IItemHandler storage : collectStorage(player)) {
            for(int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if(stack.isEmpty()) {
                    continue;
                }
                if(stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gunStack, stack)) {
                    return true;
                }
                if(stack.getItem() instanceof IAmmoBox ammoBox && ammoBox.isAmmoBoxOfGun(gunStack, stack)) {
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

    public static int consumeFromBackpacks(Player player, AbstractGunItem gun, ItemStack gunStack, int remaining) {
        int consumed = 0;
        for(IItemHandler storage : collectStorage(player)) {
            if(remaining <= 0) {
                break;
            }
            int extracted = gun.findAndExtractInventoryAmmo(storage, gunStack, remaining);
            if(extracted > 0) {
                consumed += extracted;
                remaining -= extracted;
            }
        }
        return consumed;
    }

    // Resolves the ITEM_HANDLER capability of every WARBORN backpack the
    // player can feed from: the chest armor slot (BackpackItem is an
    // ArmorItem), any Curios slot (soft dep - guarded), and the inventory.
    private static List<IItemHandler> collectStorage(Player player) {
        List<IItemHandler> out = new ArrayList<>();

        if(WarbornCompatConfig.wornEnabled()) {
            addIfBackpack(out, player.getItemBySlot(EquipmentSlot.CHEST));
            if(ModList.get().isLoaded("curios")) {
                List<ItemStack> curioStacks = new ArrayList<>();
                CuriosCompat.collectStacks(player, curioStacks);
                for(ItemStack stack : curioStacks) {
                    addIfBackpack(out, stack);
                }
            }
        }

        if(WarbornCompatConfig.inventoryEnabled()) {
            Inventory inventory = player.getInventory();
            for(int i = 0; i < inventory.getContainerSize(); i++) {
                addIfBackpack(out, inventory.getItem(i));
            }
        }

        return out;
    }

    private static void addIfBackpack(List<IItemHandler> out, ItemStack stack) {
        if(!BackpackItem.isBackpackItem(stack)) {
            return;
        }
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(out::add);
    }
}
