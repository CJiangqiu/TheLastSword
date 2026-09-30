package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.client.JustifiedDefenceFlash;
import net.the_last_sword.test.UltraTestSwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    //统一覆盖色入口同时供原版与自定义生物渲染器复用
    @Inject(method = "getOverlayCoords", at = @At("HEAD"), cancellable = true)
    private static void theLastSword$applyJustifiedDefenceFlash(LivingEntity entity,
            float whiteOverlayProgress, CallbackInfoReturnable<Integer> cir) {
        float flash = JustifiedDefenceFlash.getWhiteOverlayProgress(entity);
        if (flash > whiteOverlayProgress) {
            cir.setReturnValue(OverlayTexture.pack(OverlayTexture.u(flash),
                    OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0)));
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void theLastSword$captureEntity(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                                             MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        //防御模式的究极测试剑持有者完全不可见
        if (UltraTestSwordItem.hasDefenseSword(entity)) {
            ci.cancel();
        }
    }

}
