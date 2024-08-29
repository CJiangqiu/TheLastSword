package net.thelastsword.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.thelastsword.Static;
import net.thelastsword.client.shader.TheLastSwordShaders;
import org.jetbrains.annotations.NotNull;

public class TheLastSwordRender extends BlockEntityWithoutLevelRenderer {
    public TheLastSwordRender() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack pStack, @NotNull ItemDisplayContext pDisplayContext, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel bakedModel1 = minecraft.getModelManager().getModel(new ModelResourceLocation(Static.rl("the_last_sword"), "inventory"));
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1.0f;
        if (pDisplayContext != ItemDisplayContext.GUI) {
            yaw = (float) ((double) (minecraft.player.getYRot() * 2.0F) * 3.141592653589793D / 360.0D);
            pitch = -((float) ((double) (minecraft.player.getXRot() * 2.0F) * 3.141592653589793D / 360.0D));
        } else {
            scale = 25f;
        }
        if (TheLastSwordShaders.cosmicOpacity != null) {
            TheLastSwordShaders.cosmicOpacity.set(1.0F);
        }
        TheLastSwordShaders.cosmicYaw.set(yaw);
        TheLastSwordShaders.cosmicPitch.set(pitch);
        TheLastSwordShaders.cosmicExternalScale.set(scale);

        // 设置偏移和缩放
        TheLastSwordShaders.cosmicOffsetX.set(0.0f); // 根据需要设置偏移
        TheLastSwordShaders.cosmicOffsetY.set(0.0f); // 根据需要设置偏移
        TheLastSwordShaders.cosmicScale.set(1.0f); // 根据需要设置缩放

        final VertexConsumer cons = pBuffer.getBuffer(TheLastSwordShaders.COSMIC_RENDER_TYPE);
        itemRenderer.renderModelLists(bakedModel1, pStack, pPackedLight, pPackedOverlay, pPoseStack, cons);
        pPoseStack.popPose();
    }
}
