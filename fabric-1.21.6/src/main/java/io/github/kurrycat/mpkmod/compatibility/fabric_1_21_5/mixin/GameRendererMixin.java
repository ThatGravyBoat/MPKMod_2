package io.github.kurrycat.mpkmod.compatibility.fabric_1_21_5.mixin;

import io.github.kurrycat.mpkmod.compatibility.fabric_1_21_5.MPKMod;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderBlockEntities", at = @At("HEAD"))
    public void render(MatrixStack matrices, VertexConsumerProvider.Immediate entityVertexConsumers, VertexConsumerProvider.Immediate effectVertexConsumers, Camera camera, float tickProgress, CallbackInfo ci) {
        MPKMod.INSTANCE.eventHandler.onRenderWorldOverlay(new MatrixStack(), tickProgress);
    }
}
