package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.client.shader.TheLastEndEffect;
import net.the_last_sword.client.shader.TheLastEndRenderTypes;
import net.the_last_sword.item.TheLastSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//ItemRenderer Mixin：为最终之剑添加终焉渲染效果
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            shift = At.Shift.BEFORE
        )
    )
    private void theLastSword$renderTheLastEndEffect(
            ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
            PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight,
            int combinedOverlay, BakedModel model, CallbackInfo ci) {

        //检测是否是最终之剑
        if (!(stack.getItem() instanceof TheLastSword)) {
            return;
        }

        //检查着色器是否可用
        if (!TheLastEndEffect.isAvailable()) {
            return;
        }

        //结束当前批处理，确保基础模型已完全渲染
        if (bufferSource instanceof MultiBufferSource.BufferSource bufferSource1) {
            bufferSource1.endBatch();
        }

        //应用着色器uniforms
        TheLastEndEffect.applyUniforms();

        //使用终焉渲染类型（使用纹理图集）
        RenderType theLastEndRenderType = TheLastEndRenderTypes.createItemEffectWithAtlas();

        //渲染终焉效果层
        ItemRenderer itemRenderer = (ItemRenderer) (Object) this;
        itemRenderer.renderModelLists(model, stack, combinedLight, combinedOverlay, poseStack, bufferSource.getBuffer(theLastEndRenderType));

        //再次结束批处理，确保终焉效果已渲染
        if (bufferSource instanceof MultiBufferSource.BufferSource bufferSource1) {
            bufferSource1.endBatch();
        }
    }
}
