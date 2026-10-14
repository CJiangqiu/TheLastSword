package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.entity.EndCrystalRift;

public class EndCrystalRiftRenderer extends EntityRenderer<EndCrystalRift> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        TheLastSwordMod.MOD_ID, "textures/entities/queen_rift.png");

    public EndCrystalRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EndCrystalRift entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        QueenBlinkRenderUtil.summonRift(pose, buffers, entity.getAnimationAge(partialTick), entity.getYRot());
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(EndCrystalRift entity) {
        return TEXTURE;
    }
}
