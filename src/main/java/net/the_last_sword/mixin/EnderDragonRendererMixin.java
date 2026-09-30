package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.the_last_sword.client.JustifiedDefenceFlash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonRenderer.class)
public class EnderDragonRendererMixin {
    @Unique
    private EnderDragon the_last_sword$renderedDragon;

    @Inject(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void the_last_sword$captureRenderedDragon(EnderDragon dragon, float entityYaw,
            float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, CallbackInfo ci) {
        the_last_sword$renderedDragon = dragon;
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/OverlayTexture;pack(FZ)I"),
            index = 0
    )
    private float the_last_sword$applyJustifiedDefenceFlash(float original) {
        if (the_last_sword$renderedDragon == null) {
            return original;
        }
        float flash = JustifiedDefenceFlash.getWhiteOverlayProgress(the_last_sword$renderedDragon);
        return Math.max(flash, original);
    }
}
