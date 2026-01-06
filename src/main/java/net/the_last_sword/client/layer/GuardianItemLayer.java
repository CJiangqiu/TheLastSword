package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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
        if (bone.getName().equals(RIGHT_HAND)) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      GuardianOfSealedSpireEntity entity, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        //向外旋转90度
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));

        //根据守卫类型调整位置
        if (entity.getGuardianType() == GuardianOfSealedSpireEntity.GuardianType.ARCHER) {
            poseStack.translate(4 / 16.0, 1.0 / 16.0, -10.0 / 16.0);
        } else {
            poseStack.translate(3 / 16.0, 0, -10.0 / 16.0);
        }

        super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
