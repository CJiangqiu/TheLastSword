/*
 * VillagerTradeHandler.java
 * 处理流浪商人交易相关事件
 */
package net.the_last_sword.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.the_last_sword.init.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTradeHandler {

    //流浪商人交易
    @SubscribeEvent
    public static void registerWanderingTrades(WandererTradesEvent event) {
        // 添加到普通交易：龙蛋换终焉卷轴
        event.getGenericTrades().add(new BasicItemListing(
            new ItemStack(Items.DRAGON_EGG, 1),
            new ItemStack(ModItems.THE_LAST_END_SCROLL.get(), 1),
            1,      // 最多交易1次
            50,     // 50经验
            0.05f   // 5%价格倍数
        ));

        // 添加到普通交易：下界之星换终焉卷轴
        event.getGenericTrades().add(new BasicItemListing(
            new ItemStack(Items.NETHER_STAR, 1),
            new ItemStack(ModItems.THE_LAST_END_SCROLL.get(), 1),
            1,      // 最多交易1次
            50,     // 50经验
            0.05f   // 5%价格倍数
        ));

        // 添加到普通交易：64绿宝石换远古能量核心
        event.getGenericTrades().add(new BasicItemListing(
            new ItemStack(Items.EMERALD, 64),
            new ItemStack(ModItems.ANCIENT_ENERGY_CORE.get(), 1),
            1,      // 最多交易1次
            50,     // 50经验
            0.05f   // 5%价格倍数
        ));

        // 一次性能量电池（概率为远古能量核心的2倍，出现两次）
        for (int i = 0; i < 2; i++) {
            event.getGenericTrades().add(new BasicItemListing(
                new ItemStack(Items.EMERALD, 8),
                new ItemStack(ModItems.DISPOSABLE_ENERGY_BATTERY.get(), 1),
                64,     // 最多交易64次
                2,      // 2经验
                0.05f
            ));
        }

        // 也添加到稀有交易列表，增加出现概率
        event.getRareTrades().add(new BasicItemListing(
            new ItemStack(Items.DRAGON_EGG, 1),
            new ItemStack(ModItems.THE_LAST_END_SCROLL.get(), 1),
            1,
            50,
            0.05f
        ));

        event.getRareTrades().add(new BasicItemListing(
            new ItemStack(Items.NETHER_STAR, 1),
            new ItemStack(ModItems.THE_LAST_END_SCROLL.get(), 1),
            1,
            50,
            0.05f
        ));

        event.getRareTrades().add(new BasicItemListing(
            new ItemStack(Items.EMERALD, 64),
            new ItemStack(ModItems.ANCIENT_ENERGY_CORE.get(), 1),
            1,
            50,
            0.05f
        ));

        // 一次性能量电池（概率为远古能量核心的2倍，出现两次）
        for (int i = 0; i < 2; i++) {
            event.getRareTrades().add(new BasicItemListing(
                new ItemStack(Items.EMERALD, 8),
                new ItemStack(ModItems.DISPOSABLE_ENERGY_BATTERY.get(), 1),
                64,     // 最多交易64次
                2,      // 2经验
                0.05f
            ));
        }
    }
}