package net.the_last_sword.compat.projecte.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.the_last_sword.compat.projecte.PrimalSourceArmorItem;

public final class PrimalSourceArmorClientExtension<T extends Item & PrimalSourceArmorItem> implements IClientItemExtensions {
    private PrimalSourceArmorRenderer<T> renderer;

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                                                  EquipmentSlot slot, HumanoidModel<?> original) {
        if (renderer == null) {
            renderer = new PrimalSourceArmorRenderer<>();
        }
        renderer.prepForRender(entity, stack, slot, original);
        return renderer;
    }
}
