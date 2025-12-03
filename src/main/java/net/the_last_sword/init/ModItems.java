package net.the_last_sword.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.item.*;
import net.the_last_sword.test.UltraTestSwordItem;
import net.minecraft.world.item.Rarity;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<Item> DRAGON_CRYSTAL = ITEMS.register("dragon_crystal",
        DragonCrystal::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_UPGRADE_TEMPLATE = ITEMS.register("dragon_crystal_upgrade_template",
        DragonCrystalUpgradeTemplate::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_SMITHING_TABLE = ITEMS.register("dragon_crystal_smithing_table",
        DragonCrystalSmithingTableBlockItem::new
    );

    public static final RegistryObject<Item> DRAGON_CRYSTAL_SWORD = ITEMS.register("dragon_crystal_sword",
        DragonCrystalSword::new
    );

    public static final RegistryObject<Item> DRAGON_SWORD = ITEMS.register("dragon_sword",
        DragonSword::new
    );

    public static final RegistryObject<Item> THE_LAST_SWORD = ITEMS.register("the_last_sword",
        TheLastSword::new
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

    //终焉剑灵刷怪蛋
    public static final RegistryObject<Item> THE_LAST_END_SWORD_WRAITH_SPAWN_EGG = ITEMS.register("the_last_end_sword_wraith_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.THE_LAST_END_SWORD_WRAITH, -13434829, -16777216, new Item.Properties())
    );

    //13级终焉剑灵刷怪蛋
    public static final RegistryObject<Item> THE_LAST_END_SWORD_WRAITH_LEVEL_13_SPAWN_EGG = ITEMS.register("the_last_end_sword_wraith_level_13_spawn_egg",
        TheLastEndSwordWraithLevel13SpawnEgg::new
    );

    //测试实体刷怪蛋
    public static final RegistryObject<Item> TEST_ENTITY_SPAWN_EGG = ITEMS.register("test_entity_spawn_egg",
        () -> new ForgeSpawnEggItem(ModEntities.TEST_ENTITY, -1, -1, new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
