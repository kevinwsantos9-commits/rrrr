package com.nivek.animated.mixin;

import com.nivek.animated.client.Anim;
import com.nivek.animated.client.FirstPersonSword;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void nivek$head(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                            VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.push();
        if (hand == Hand.MAIN_HAND && Anim.category(item) <= 4) {
            FirstPersonSword.apply(player, tickDelta, swingProgress, item, matrices);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void nivek$tail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                            VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
