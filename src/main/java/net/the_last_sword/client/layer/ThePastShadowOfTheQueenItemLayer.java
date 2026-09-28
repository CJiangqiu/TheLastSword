package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.entity.ThePastShadowOfTheQueenEntity;
import net.the_last_sword.init.ModItems;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class ThePastShadowOfTheQueenItemLayer extends BlockAndItemGeoLayer<ThePastShadowOfTheQueenEntity> {
    private final ItemStack lightningSpear = new ItemStack(ModItems.LIGHTNING_SPEAR.get());

    public ThePastShadowOfTheQueenItemLayer(GeoRenderer<ThePastShadowOfTheQueenEntity> renderer) {
        super(renderer);
    }

    @Override
    @Nullable
    protected ItemStack getStackForBone(GeoBone bone, ThePastShadowOfTheQueenEntity entity) {
        if (!bone.getName().equals("right_hand")) {
            return null;
        }
        if (entity.isDying()) {
            ItemStack deathItem = entity.getMainHandItem();
            return deathItem.isEmpty() ? null : deathItem;
        }
        return entity.isLightningSpearVisible() ? lightningSpear : null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack,
                                                          ThePastShadowOfTheQueenEntity entity) {
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                      ThePastShadowOfTheQueenEntity entity, MultiBufferSource bufferSource,
                                      float partialTick, int packedLight, int packedOverlay) {
        // 手部骨骼的枢轴在肘部，沿前臂下移到掌心后再应用物品变换。
        poseStack.translate(0.0D, -7.0D / 16.0D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        super.renderStackForBone(poseStack, bone, stack, entity, bufferSource,
                partialTick, packedLight, packedOverlay);
    }

}
