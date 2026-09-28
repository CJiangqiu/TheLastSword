package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.QueenBlinkBlade;
import net.the_last_sword.entity.QueenEnhancedBlade;

public class QueenBlinkBladeRenderer extends EntityRenderer<QueenBlinkBlade> {
    private final boolean enhanced;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID,
            "textures/entities/the_past_shadow_of_the_queen.png");

    public QueenBlinkBladeRenderer(EntityRendererProvider.Context context) {
        this(context, false);
    }

    public QueenBlinkBladeRenderer(EntityRendererProvider.Context context, boolean enhanced) {
        super(context);
        this.enhanced = enhanced;
    }

    @Override
    public void render(QueenBlinkBlade entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        if (entity instanceof QueenEnhancedBlade blade) {
            pose.translate(0, entity.bladeHeight() * 0.5, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(blade.bladeRoll()));
            pose.translate(0, -entity.bladeHeight() * 0.5, 0);
        }
        QueenBlinkRenderUtil.blade(pose, buffers, entity.bladeWidth(), entity.bladeHeight(), entity.bladeLength(), enhanced);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(QueenBlinkBlade entity) {
        return TEXTURE;
    }
}
