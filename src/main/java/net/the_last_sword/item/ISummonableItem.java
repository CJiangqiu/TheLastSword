package net.the_last_sword.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 可召唤剑灵的物品接口
 * 实现此接口的物品可以通过 WraithSummonManager 召唤剑灵
 */
public interface ISummonableItem {

    //获取召唤冷却时间（tick）
    int getSummonCooldownTicks();

    //是否可以召唤（默认实现：总是可以召唤）
    default boolean canSummon(Player player, ItemStack stack) {
        return true;
    }
}
