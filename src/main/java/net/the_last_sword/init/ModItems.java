package net.the_last_sword.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.cataclysm.CataclysmItemsRegistry;
import net.the_last_sword.compat.curios.CuriosItemsRegistry;
import net.the_last_sword.compat.lucky_block.TheLastEndLuckyBlockDisplayItem;
import net.the_last_sword.item.*;
import net.the_last_sword.test.UltraTestSwordItem;
import net.minecraft.world.item.Rarity;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, TheLastSwordMod.MOD_ID);

    //终焉卷轴 - 知识书
    public static final RegistryObject<Item> THE_LAST_END_SCROLL = ITEMS.register("the_last_end_scroll",
        TheLastEndScroll::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL = ITEMS.register("dragon_crystal",
        DragonCrystal::new
    );

    public static final RegistryObject<Item> DISPOSABLE_ENERGY_BATTERY = ITEMS.register("disposable_energy_battery",
        DisposableEnergyBattery::new
    );

    public static final RegistryObject<Item> ANCIENT_ENERGY_CORE = ITEMS.register("ancient_energy_core",
        AncientEnergyCore::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_UPGRADE_TEMPLATE = ITEMS.register("dragon_crystal_upgrade_template",
        DragonCrystalUpgradeTemplate::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_SMITHING_TABLE = ITEMS.register("dragon_crystal_smithing_table",
        DragonCrystalSmithingTableBlockItem::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_ENCHANTING_TABLE = ITEMS.register("dragon_crystal_enchanting_table",
        () -> new net.the_last_sword.item.display.DragonCrystalEnchantingTableDisplayItem(
            ModBlocks.DRAGON_CRYSTAL_ENCHANTING_TABLE.get(),
            new Item.Properties()
                .rarity(Rarity.UNCOMMON)
                .fireResistant()
        )
    );

    //终焉幸运方块（幸运方块本体 mod 加载时才注册, 见 registerConditionalItems）
    public static RegistryObject<Item> THE_LAST_END_LUCKY_BLOCK;

    public static final RegistryObject<Item> DRAGON_CRYSTAL_SWORD = ITEMS.register("dragon_crystal_sword",
        DragonCrystalSword::new
    );

    public static final RegistryObject<Item> DRAGON_SWORD = ITEMS.register("dragon_sword",
        DragonSword::new
    );

    public static final RegistryObject<Item> THE_LAST_SWORD = ITEMS.register("the_last_sword",
        TheLastSword::new
    );

    public static final RegistryObject<Item> THE_LAST_SWORD_YOU_NEVER_FORGOT = ITEMS.register("the_last_sword_you_never_forgot",
        TheLastSwordYouNeverForgot::new
    );

    public static final RegistryObject<Item> ULTRA_TEST_SWORD = ITEMS.register("ultra_test_sword",
        UltraTestSwordItem::new
    );

    //魂石
    public static final RegistryObject<Item> DRAGON_CRYSTAL_SOUL_STONE = ITEMS.register("dragon_crystal_soul_stone",
        DragonCrystalSoulStone::new
    );

    public static final RegistryObject<Item> SWORD_SOUL_STONE = ITEMS.register("sword_soul_stone",
        SwordSoulStone::new
    );

    //龙魂灯笼
    public static final RegistryObject<Item> DRAGON_SOUL_LANTERN = ITEMS.register("dragon_soul_lantern",
        () -> new DragonSoulLanternItem(ModBlocks.DRAGON_SOUL_LANTERN.get(), new Item.Properties().stacksTo(64).rarity(Rarity.RARE).fireResistant())
    );

    //龙水晶盔甲
    public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_HELMET = ITEMS.register("dragon_crystal_armor_helmet",
        DragonCrystalArmorItem.Helmet::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_CHESTPLATE = ITEMS.register("dragon_crystal_armor_chestplate",
        DragonCrystalArmorItem.Chestplate::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_LEGGINGS = ITEMS.register("dragon_crystal_armor_leggings",
        DragonCrystalArmorItem.Leggings::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_ARMOR_BOOTS = ITEMS.register("dragon_crystal_armor_boots",
        DragonCrystalArmorItem.Boots::new
    );

    //龙之盔甲
    public static final RegistryObject<Item> DRAGON_ARMOR_HELMET = ITEMS.register("dragon_armor_helmet",
        DragonArmorItem.Helmet::new
    );

    public static final RegistryObject<Item> DRAGON_ARMOR_CHESTPLATE = ITEMS.register("dragon_armor_chestplate",
        DragonArmorItem.Chestplate::new
    );

    public static final RegistryObject<Item> DRAGON_ARMOR_LEGGINGS = ITEMS.register("dragon_armor_leggings",
        DragonArmorItem.Leggings::new
    );

    public static final RegistryObject<Item> DRAGON_ARMOR_BOOTS = ITEMS.register("dragon_armor_boots",
        DragonArmorItem.Boots::new
    );

    //测试实体刷怪蛋
    public static final RegistryObject<Item> TEST_ENTITY_SPAWN_EGG = ITEMS.register("test_entity_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.TEST_ENTITY, -1, -1, new Item.Properties())
    );

    //拜龙教教徒刷怪蛋（深紫色 + 紫色）
    public static final RegistryObject<Item> DRAGON_CULTIST_SPAWN_EGG = ITEMS.register("dragon_cultist_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.DRAGON_CULTIST, 0x4B0082, 0x9932CC, new Item.Properties())
    );

    //拜龙教圣骑士刷怪蛋（深紫色 + 黑色）
    public static final RegistryObject<Item> DRAGON_CULT_PALADIN_SPAWN_EGG = ITEMS.register("dragon_cult_paladin_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.DRAGON_CULT_PALADIN, 0x4B0082, 0x1A1A1A, new Item.Properties())
    );

    //封印尖塔守卫刷怪蛋（深灰色 + 紫色）
    public static final RegistryObject<Item> GUARDIAN_OF_SEALED_SPIRE_SPAWN_EGG = ITEMS.register("guardian_of_sealed_spire_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.GUARDIAN_OF_SEALED_SPIRE, 0x4B4B4B, 0x8B00FF, new Item.Properties())
    );

    //守卫剑士刷怪蛋（深灰色 + 蓝色）
    public static final RegistryObject<Item> GUARDIAN_SABER_SPAWN_EGG = ITEMS.register("guardian_saber_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.GUARDIAN_SABER, 0x4B4B4B, 0x0080FF, new Item.Properties())
    );

    //守卫狂战士刷怪蛋（深灰色 + 红色）
    public static final RegistryObject<Item> GUARDIAN_BERSERKER_SPAWN_EGG = ITEMS.register("guardian_berserker_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.GUARDIAN_BERSERKER, 0x4B4B4B, 0xDC143C, new Item.Properties())
    );

    //守卫弓箭手刷怪蛋（深灰色 + 绿色）
    public static final RegistryObject<Item> GUARDIAN_ARCHER_SPAWN_EGG = ITEMS.register("guardian_archer_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.GUARDIAN_ARCHER, 0x4B4B4B, 0x00AA00, new Item.Properties())
    );

    //迷失战魂刷怪蛋（黑色 + 深灰色）
    public static final RegistryObject<Item> LOST_WRAITH_SPAWN_EGG = ITEMS.register("lost_wraith_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.LOST_WRAITH, 0x000000, 0x4B4B4B, new Item.Properties())
    );

    //终焉剑灵刷怪蛋（黑色 + 白色）
    public static final RegistryObject<Item> THE_LAST_END_SWORD_WRAITH_SPAWN_EGG = ITEMS.register("the_last_end_sword_wraith_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.THE_LAST_END_SWORD_WRAITH, 0x000000, 0xFFFFFF, new Item.Properties())
    );

    //13级终焉剑灵生成蛋（自定义物品）
    public static final RegistryObject<Item> THE_LAST_END_SWORD_WRAITH_LEVEL_13_SPAWN_EGG = ITEMS.register("the_last_end_sword_wraith_level_13_spawn_egg",
        TheLastEndSwordWraithLevel13SpawnEgg::new
    );

    //女皇的逝去之影刷怪蛋（紫色 + 浅紫色）
    public static final RegistryObject<Item> THE_PAST_SHADOW_OF_THE_QUEEN_SPAWN_EGG = ITEMS.register("the_past_shadow_of_the_queen_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.THE_PAST_SHADOW_OF_THE_QUEEN, 0x800080, 0xDDA0DD, new Item.Properties())
    );

    //Curios饰品（前置mod，直接注册）
    public static final RegistryObject<Item> DRAGON_CRYSTAL_RING = CuriosItemsRegistry.registerDragonCrystalRing(ITEMS);
    public static final RegistryObject<Item> DRAGON_CRYSTAL_NECKLACE = CuriosItemsRegistry.registerDragonCrystalNecklace(ITEMS);
    public static final RegistryObject<Item> DRAGON_CRYSTAL_CROWN = CuriosItemsRegistry.registerDragonCrystalCrown(ITEMS);
    public static final RegistryObject<Item> WINGS_THAT_COVER_THE_WORLD = CuriosItemsRegistry.registerWingsThatCoverTheWorld(ITEMS);
    public static final RegistryObject<Item> EXTREME_LIFE_SUPPORT_DEVICE = CuriosItemsRegistry.registerExtremeLifeSupportDevice(ITEMS);
    public static final RegistryObject<Item> DIMENSION_EXPLORER = CuriosItemsRegistry.registerDimensionExplorer(ITEMS);
    public static final RegistryObject<Item> THE_GIVERS_PAIN = CuriosItemsRegistry.registerTheGiversPain(ITEMS);

    //Cataclysm联动物品（条件注册）
    public static RegistryObject<Item> ANCIENT_REMNANT_MEDAL;
    public static RegistryObject<Item> ENDER_GUARDIAN_MEDAL;
    public static RegistryObject<Item> IGNIS_MEDAL;
    public static RegistryObject<Item> MALEDICTUS_MEDAL;
    public static RegistryObject<Item> NETHERITE_MONSTROSITY_MEDAL;
    public static RegistryObject<Item> THE_HARBINGER_MEDAL;
    public static RegistryObject<Item> THE_LEVIATHAN_MEDAL;
    public static RegistryObject<Item> SCYLLA_MEDAL;

    public static void register(IEventBus eventBus) {
        //注册条件物品
        registerConditionalItems();
        //注册到事件总线
        ITEMS.register(eventBus);
    }

    //注册联动物品（直接调用，Java懒加载保证不会在未加载时执行）
    private static void registerConditionalItems() {
        //灾变奖章
        if (CompatCheck.isCataclysmLoaded()) {
            ANCIENT_REMNANT_MEDAL = CataclysmItemsRegistry.registerAncientRemnantMedal(ITEMS);
            ENDER_GUARDIAN_MEDAL = CataclysmItemsRegistry.registerEnderGuardianMedal(ITEMS);
            IGNIS_MEDAL = CataclysmItemsRegistry.registerIgnisMedal(ITEMS);
            MALEDICTUS_MEDAL = CataclysmItemsRegistry.registerMaledictusMedal(ITEMS);
            NETHERITE_MONSTROSITY_MEDAL = CataclysmItemsRegistry.registerNetheriteMonstrosityMedal(ITEMS);
            THE_HARBINGER_MEDAL = CataclysmItemsRegistry.registerTheHarbingerMedal(ITEMS);
            THE_LEVIATHAN_MEDAL = CataclysmItemsRegistry.registerTheLeviathanMedal(ITEMS);
            SCYLLA_MEDAL = CataclysmItemsRegistry.registerScyllaMedal(ITEMS);
        }
        //幸运方块联动
        if (CompatCheck.isLuckyBlockLoaded()) {
            THE_LAST_END_LUCKY_BLOCK = ITEMS.register("the_last_end_lucky_block",
                () -> new TheLastEndLuckyBlockDisplayItem(
                    ModBlocks.THE_LAST_END_LUCKY_BLOCK.get(),
                    new Item.Properties()
                        .rarity(Rarity.RARE)
                )
            );
        }
    }
}
