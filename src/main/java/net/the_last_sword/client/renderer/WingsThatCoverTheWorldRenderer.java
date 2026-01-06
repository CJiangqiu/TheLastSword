package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.client.model.WingsThatCoverTheWorldModel;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

//覆世之翼渲染器 - 在玩家背部渲染翅膀模型
public class WingsThatCoverTheWorldRenderer implements ICurioRenderer.ModelRender<WingsThatCoverTheWorldModel<LivingEntity>> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("the_last_sword", "textures/entity/curios/wings_that_cover_the_world.png");

    private final WingsThatCoverTheWorldModel<LivingEntity> model;

    public WingsThatCoverTheWorldRenderer() {
        this.model = new WingsThatCoverTheWorldModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(WingsThatCoverTheWorldModel.LAYER_LOCATION)
        );
    }

    @Override
    public void prepareModel(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<LivingEntity, EntityModel<LivingEntity>> renderLayerParent,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {

        //跟随玩家身体旋转（蹲下时）
        ICurioRenderer.rotateIfSneaking(poseStack, slotContext.entity());
        ICurioRenderer.translateIfSneaking(poseStack, slotContext.entity());

        //设置翅膀动画
        this.model.setupAnim(slotContext.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    public WingsThatCoverTheWorldModel<LivingEntity> getModel(ItemStack stack, SlotContext slotContext) {
        return this.model;
    }

    @Override
    public ResourceLocation getModelTexture(ItemStack stack, SlotContext slotContext) {
        return TEXTURE;
    }
}
