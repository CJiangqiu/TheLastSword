package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LightningSpearItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final long ANIMATION_START_NANOS = System.nanoTime();

    public LightningSpearItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext,
                             @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                             int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LightningSpearRenderUtil.render(poseStack, bufferSource, animationAge());
        poseStack.popPose();
    }

    private static float animationAge() {
        return (System.nanoTime() - ANIMATION_START_NANOS) / 50_000_000.0F;
    }
}
