package com.poseidon.tacz_warborn_compat.mixin;

import com.raiiiden.warborn.common.object.capability.BackpackCapabilityProvider;
import com.raiiiden.warborn.common.object.capability.BackpackItemStackHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackpackCapabilityProvider.class, remap = false)
public class BackpackCapabilityProviderMixin {

    @Shadow
    private BackpackItemStackHandler handler;

    @Shadow
    private ItemStack stack;

    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void onSerializeNBT(CallbackInfoReturnable<CompoundTag> cir) {
        if (this.stack != null && cir.getReturnValue() != null) {
            this.stack.getOrCreateTag().put("BackpackCap", cir.getReturnValue());
        }
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void onDeserializeNBT(CompoundTag nbt, CallbackInfo ci) {
        if (this.stack != null && this.handler != null) {
            this.stack.getOrCreateTag().put("BackpackCap", this.handler.serializeNBT());
        }
    }
}
