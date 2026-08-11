package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.entity.DragonCultistEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

//拜龙教教徒物品渲染层
public class DragonCultistItemLayer extends BlockAndItemGeoLayer<DragonCultistEntity> {

    //骨骼名称
    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    //手持物品相对手部骨骼的偏移（像素）
    private static final float ITEM_OFFSET_X = 1.0F;
    private static final float ITEM_OFFSET_Z = -3.75F;

    public DragonCultistItemLayer(GeoRenderer<DragonCultistEntity> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, DragonCultistEntity entity) {
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
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, DragonCultistEntity entity) {
        if (bone.getName().equals(RIGHT_HAND)) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      DragonCultistEntity entity, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        //向外旋转90度
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.translate(ITEM_OFFSET_X / 16.0, 0, ITEM_OFFSET_Z / 16.0);

        super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
