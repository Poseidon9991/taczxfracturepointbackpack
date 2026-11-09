package com.poseidon.tacz_warborn_compat.mixin;

import com.raiiiden.warborn.common.item.BackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BackpackItem.class)
public abstract class BackpackItemMixin extends ArmorItem {

    public BackpackItemMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }

    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        CompoundTag tag = super.getShareTag(stack);
        CompoundTag out = tag == null ? new CompoundTag() : tag.copy();
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if (handler instanceof INBTSerializable<?> serializable) {
                out.put("BackpackCap", serializable.serializeNBT());
            }
        });
        return out;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
        if (nbt != null && nbt.contains("BackpackCap", Tag.TAG_COMPOUND)) {
            stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                if (handler instanceof INBTSerializable<?> serializable) {
                    ((INBTSerializable<CompoundTag>) serializable).deserializeNBT(nbt.getCompound("BackpackCap"));
                }
            });
        }
    }
}
