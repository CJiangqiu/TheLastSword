package net.the_last_sword.compat.projecte.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.the_last_sword.compat.projecte.PrimalSourceArmorItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PrimalSourceArmorRenderer<T extends Item & PrimalSourceArmorItem> extends GeoArmorRenderer<T> {
    public PrimalSourceArmorRenderer() {
        super(new PrimalSourceArmorModel<>());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T item, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        // 部位外层还承载动画，必须连同整棵子树一起过滤，避免单件装备显示整套装饰。
        EquipmentSlot requiredSlot = switch (bone.getName()) {
            case "crown_of_countless_stars" -> EquipmentSlot.HEAD;
            case "robe_of_the_primal_source" -> EquipmentSlot.CHEST;
            case "leggings_of_earths_blessing_left", "leggings_of_earths_blessing_right" -> EquipmentSlot.LEGS;
            case "boots_of_the_deep_abyss_left", "boots_of_the_deep_abyss_right" -> EquipmentSlot.FEET;
            default -> null;
        };
        if (requiredSlot != null && requiredSlot != getCurrentSlot()) {
            return;
        }
        super.renderRecursively(poseStack, item, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
