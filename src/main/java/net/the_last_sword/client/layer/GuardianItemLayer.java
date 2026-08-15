package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.the_last_sword.entity.GuardianOfSealedSpireEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import net.minecraft.client.renderer.MultiBufferSource;

import javax.annotation.Nullable;

//封印尖塔守卫物品渲染层
public class GuardianItemLayer extends BlockAndItemGeoLayer<GuardianOfSealedSpireEntity> {

    //骨骼名称
    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    //手持物相对新版手腕骨骼的偏移（像素）
    private static final float ITEM_OFFSET_Z = -3.75F;
    private static final float ARCHER_EXTRA_OFFSET = 1.0F;
    private static final float ITEM_FORWARD_OFFSET = 1.0F;
    private static final float ITEM_UPWARD_ROTATION = 10.0F;

    //盾面朝向：-90度为盾面朝身体外侧、握把贴身，180则正对前方
    private static final float SHIELD_FACE_ROTATION = -90.0F;

    public GuardianItemLayer(GeoRenderer<GuardianOfSealedSpireEntity> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, GuardianOfSealedSpireEntity entity) {
        //右手骨骼渲染主手物品
        if (bone.getName().equals(RIGHT_HAND)) {
            return entity.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        //左手骨骼渲染副手物品
        if (bone.getName().equals(LEFT_HAND)) {
            return entity.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        return null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, GuardianOfSealedSpireEntity entity) {
        //盾牌直接以left_hand枢轴为挂点，绕过原版第三人称左手自带的平移
        if (stack.is(Items.SHIELD)) {
            return ItemDisplayContext.NONE;
        }
        if (bone.getName().equals(RIGHT_HAND)) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      GuardianOfSealedSpireEntity entity, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        //新版left_hand枢轴就是盾牌挂点：盾牌模型本身已是竖直的，只需转出盾面朝外、握把贴身的持盾姿态
        if (stack.is(Items.SHIELD)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(SHIELD_FACE_ROTATION));
            //抵消ItemRenderer对方块模型的半格居中平移，让盾牌握把落在枢轴上
            poseStack.translate(0.5, 0.5, 0.5);
            super.renderStackForBone(poseStack, bone, stack, entity, bufferSource,
                    partialTick, packedLight, packedOverlay);
            return;
        }

        //先沿守卫正前方移动，再将物品朝上额外旋转10度
        poseStack.translate(0, 0, -ITEM_FORWARD_OFFSET / 16.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F + ITEM_UPWARD_ROTATION));

        //弓的模型比近战武器更宽，保留少量额外偏移
        if (entity.getGuardianType() == GuardianOfSealedSpireEntity.GuardianType.ARCHER) {
            poseStack.translate(ARCHER_EXTRA_OFFSET / 16.0,
                    ARCHER_EXTRA_OFFSET / 16.0, ITEM_OFFSET_Z / 16.0);
        } else {
            poseStack.translate(0, 0, ITEM_OFFSET_Z / 16.0);
        }

        super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
