/*
 * VillagerTradeHandler.java
 * 处理村民和流浪商人交易相关事件
 */
package net.the_last_sword.event;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;

import net.the_last_sword.init.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTradeHandler {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        // 检查职业是否为盔甲匠、武器匠或工具匠
        if (event.getType() == VillagerProfession.ARMORER ||
            event.getType() == VillagerProfession.WEAPONSMITH ||
            event.getType() == VillagerProfession.TOOLSMITH) {

            // 确保5级交易槽位存在
            if (!event.getTrades().containsKey(5)) {
                event.getTrades().put(5, new java.util.ArrayList<>());
            }

            ItemStack buyingItem1 = new ItemStack(Items.EMERALD, 1); // 1个绿宝石
            ItemStack buyingItem2 = ItemStack.EMPTY;

            if (event.getType() == VillagerProfession.ARMORER) {
                // 盔甲匠：1绿宝石 + 钻石头盔换终焉卷轴
                buyingItem2 = new ItemStack(Items.DIAMOND_HELMET);
            } else if (event.getType() == VillagerProfession.WEAPONSMITH) {
                // 武器匠：1绿宝石 + 钻石剑换终焉卷轴
                buyingItem2 = new ItemStack(Items.DIAMOND_SWORD);
            } else if (event.getType() == VillagerProfession.TOOLSMITH) {
                // 工具匠：64绿宝石 + 钻石镐换终焉卷轴
                buyingItem2 = new ItemStack(Items.DIAMOND_PICKAXE);
            }

            // 添加交易：1绿宝石 + 对应钻石物品 = 1终焉卷轴，100经验
            event.getTrades().get(5).add(new BasicItemListing(buyingItem1, buyingItem2,  ModItems.THE_LAST_END_SCROLL.get().getDefaultInstance(), 1, 100, 0
            ));
        }
    }

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