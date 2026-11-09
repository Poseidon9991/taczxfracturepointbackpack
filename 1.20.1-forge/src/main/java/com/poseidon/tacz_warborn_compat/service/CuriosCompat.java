package com.poseidon.tacz_warborn_compat.service;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;

// Every Curios-API reference lives in this class so it only loads behind a
// ModList.isLoaded("curios") guard — WARBORN treats Curios as optional and
// so do we. Called from BackpackAmmoService.collectStorage.
public final class CuriosCompat {

    private CuriosCompat() {
    }

    // Collects the raw ItemStacks sitting in every Curios slot. The caller
    // filters them through BackpackItem.isBackpackItem, so this stays
    // agnostic about what a backpack is.
    //
    // getCurios() is walked by hand: it covers every registered slot type,
    // which is all we need since the caller does the filtering. Stacks are
    // deduplicated by identity so a backpack reachable through two paths is
    // only scanned once.
    public static void collectStacks(Player player, List<ItemStack> out) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.getCurios().values().forEach(stacksHandler -> {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for(int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if(!stack.isEmpty() && !out.contains(stack)) {
                        out.add(stack);
                    }
                }
            });
        });
    }
}
