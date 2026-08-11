package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.GroundRuptureFragmentEntity;

public class GroundRuptureFragmentRenderer extends EntityRenderer<GroundRuptureFragmentEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public GroundRuptureFragmentRenderer(EntityRendererProvider.Context context) {
        super(context);
        blockRenderer = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public void render(GroundRuptureFragmentEntity fragment, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int seed = fragment.getVisualSeed();
        float age = fragment.tickCount + partialTick;
        float shrink = age > 13.0F ? Mth.clamp(1.0F - (age - 13.0F) / 8.0F, 0.35F, 1.0F) : 1.0F;
        float scale = fragment.getFragmentScale() * shrink;

        float scaleX = scale * (0.72F + component(seed, 0) * 0.38F);
        float scaleY = scale * (0.48F + component(seed, 8) * 0.52F);
        float scaleZ = scale * (0.72F + component(seed, 16) * 0.38F);
        float spinDirection = (seed & 1) == 0 ? 1.0F : -1.0F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(component(seed, 4) * 45.0F + age * 7.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(component(seed, 12) * 360.0F + age * 5.0F * spinDirection));
        poseStack.mulPose(Axis.ZP.rotationDegrees(component(seed, 20) * 45.0F - age * 6.0F * spinDirection));
        poseStack.scale(scaleX, scaleY, scaleZ);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        blockRenderer.renderSingleBlock(fragment.getBlockState(), poseStack, buffer,
            packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(fragment, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static float component(int seed, int shift) {
        return ((seed >>> shift) & 0xFF) / 255.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(GroundRuptureFragmentEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
