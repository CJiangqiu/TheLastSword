package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.init.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected M model;

    @Shadow
    protected abstract RenderType getRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowing);

    @Unique
    private T the_last_sword$currentEntity;

    //在render方法开始时捕获当前实体
    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD")
    )
    private void theLastSword$captureEntity(T entity, float entityYaw, float partialTicks, PoseStack poseStack,
                                             MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        the_last_sword$currentEntity = entity;
    }

    //虚化效果：使用半透明渲染类型
    @Inject(
        method = "getRenderType",
        at = @At("HEAD"),
        cancellable = true
    )
    protected void theLastSword$modifyRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (entity.hasEffect(ModEffects.PHASING.get())) {
            LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
            cir.setReturnValue(RenderType.entityTranslucent(renderer.getTextureLocation(entity)));
        }
    }

    //虚化效果：修改实体本体的alpha参数为0.4（40%透明）
    @ModifyArg(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
        index = 7
    )
    private float theLastSword$modifyEntityAlpha(float originalAlpha) {
        if (the_last_sword$currentEntity != null && the_last_sword$currentEntity.hasEffect(ModEffects.PHASING.get())) {
            return 0.4f;
        }
        return originalAlpha;
    }
}
