package net.the_last_sword.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.init.ModItems;

// 公共保护入口只比较注册物品，避免加载可选联动的护甲实现。
public final class PrimalSourceEquipment {
    private PrimalSourceEquipment() {
    }

    public static boolean isFullSet(LivingEntity entity) {
        return entity != null && CompatCheck.isTransmutationLoaded()
            && matches(entity.getItemBySlot(EquipmentSlot.HEAD), ModItems.CROWN_OF_COUNTLESS_STARS)
            && matches(entity.getItemBySlot(EquipmentSlot.CHEST), ModItems.ROBE_OF_THE_PRIMAL_SOURCE)
            && matches(entity.getItemBySlot(EquipmentSlot.LEGS), ModItems.LEGGINGS_OF_EARTHS_BLESSING)
            && matches(entity.getItemBySlot(EquipmentSlot.FEET), ModItems.BOOTS_OF_THE_DEEP_ABYSS);
    }

    public static boolean isArmor(ItemStack stack) {
        return CompatCheck.isTransmutationLoaded()
            && (matches(stack, ModItems.CROWN_OF_COUNTLESS_STARS)
                || matches(stack, ModItems.ROBE_OF_THE_PRIMAL_SOURCE)
                || matches(stack, ModItems.LEGGINGS_OF_EARTHS_BLESSING)
                || matches(stack, ModItems.BOOTS_OF_THE_DEEP_ABYSS));
    }

    private static boolean matches(ItemStack stack, RegistryObject<Item> item) {
        return !stack.isEmpty() && item != null && item.isPresent() && stack.is(item.get());
    }
}
