package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.the_last_sword.entity.LightningSpearProjectile;
import org.jetbrains.annotations.NotNull;

public class LightningSpearProjectileRenderer extends EntityRenderer<LightningSpearProjectile> {
    public LightningSpearProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(@NotNull LightningSpearProjectile projectile, float entityYaw, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, projectile.yRotO, projectile.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTick, projectile.xRotO, projectile.getXRot()) + 90.0F));
        LightningSpearRenderUtil.render(poseStack, bufferSource, projectile.tickCount + partialTick);
        poseStack.popPose();
        super.render(projectile, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LightningSpearProjectile projectile) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
