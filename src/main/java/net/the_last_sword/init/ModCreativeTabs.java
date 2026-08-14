package net.the_last_sword.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.CompatCheck;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheLastSwordMod.MOD_ID);

    //最终之剑物品栏
    public static final RegistryObject<CreativeModeTab> THE_LAST_SWORD_TAB = CREATIVE_MODE_TABS.register("the_last_sword_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.the_last_sword.the_last_sword_tab"))
            .icon(() -> new ItemStack(ModItems.THE_LAST_SWORD.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.THE_LAST_END_SCROLL.get());
                output.accept(ModItems.DRAGON_CRYSTAL.get());
                output.accept(ModItems.DRAGON_CRYSTAL_UPGRADE_TEMPLATE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get());
                output.accept(ModItems.KNIGHT_GREATSWORD.get());
                output.accept(ModItems.PRIEST_STAFF.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SWORD.get());
                output.accept(ModItems.DRAGON_SWORD.get());
                output.accept(ModItems.THE_LAST_SWORD.get());
                output.accept(ModItems.DRAGON_CRYSTAL_RING.get());
                output.accept(ModItems.DRAGON_CRYSTAL_NECKLACE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_CROWN.get());
                output.accept(ModItems.WINGS_THAT_COVER_THE_WORLD.get());
                output.accept(ModItems.EXTREME_LIFE_SUPPORT_DEVICE.get());
                output.accept(ModItems.DIMENSION_EXPLORER.get());
                output.accept(ModItems.THE_GIVERS_PAIN.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_CHESTPLATE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_LEGGINGS.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_BOOTS.get());
                output.accept(ModItems.DISPOSABLE_ENERGY_BATTERY.get().getDefaultInstance());
                output.accept(ModItems.ANCIENT_ENERGY_CORE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ENCHANTING_TABLE.get());
                output.accept(ModItems.DRAGON_ARMOR_HELMET.get());
                output.accept(ModItems.DRAGON_ARMOR_CHESTPLATE.get());
                output.accept(ModItems.DRAGON_ARMOR_LEGGINGS.get());
                output.accept(ModItems.DRAGON_ARMOR_BOOTS.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SOUL_STONE.get());
                output.accept(ModItems.SWORD_SOUL_STONE.get());
                output.accept(ModItems.DRAGON_SOUL_LANTERN.get());
                output.accept(ModItems.DRAGON_CULTIST_SPAWN_EGG.get());
                output.accept(ModItems.DRAGON_CULT_PALADIN_SPAWN_EGG.get());
                output.accept(ModItems.DRAGON_CULT_PRIEST_SPAWN_EGG.get());
                output.accept(ModItems.GUARDIAN_OF_SEALED_SPIRE_SPAWN_EGG.get());
                output.accept(ModItems.GUARDIAN_SABER_SPAWN_EGG.get());
                output.accept(ModItems.GUARDIAN_BERSERKER_SPAWN_EGG.get());
                output.accept(ModItems.GUARDIAN_ARCHER_SPAWN_EGG.get());
                output.accept(ModItems.LOST_WRAITH_SPAWN_EGG.get());
                output.accept(ModItems.THE_LAST_END_SWORD_WRAITH_SPAWN_EGG.get());
                output.accept(ModItems.THE_LAST_END_SWORD_WRAITH_LEVEL_13_SPAWN_EGG.get());
                output.accept(ModItems.THE_PAST_SHADOW_OF_THE_QUEEN_SPAWN_EGG.get());
                //测试物品
                output.accept(ModItems.ULTRA_TEST_SWORD.get());
                output.accept(ModItems.TEST_ENTITY_SPAWN_EGG.get());

                //Cataclysm联动奖章
                if (CompatCheck.isCataclysmLoaded() && ModItems.ANCIENT_REMNANT_MEDAL != null) {
                    output.accept(ModItems.ANCIENT_REMNANT_MEDAL.get());
                    output.accept(ModItems.ENDER_GUARDIAN_MEDAL.get());
                    output.accept(ModItems.IGNIS_MEDAL.get());
                    output.accept(ModItems.MALEDICTUS_MEDAL.get());
                    output.accept(ModItems.NETHERITE_MONSTROSITY_MEDAL.get());
                    output.accept(ModItems.THE_HARBINGER_MEDAL.get());
                    output.accept(ModItems.THE_LEVIATHAN_MEDAL.get());
                    output.accept(ModItems.SCYLLA_MEDAL.get());
                }

                //LuckyBlock联动方块: 基础(luck=0) / 极幸运(+100) / 极不幸(-100) 三个变种; 仅在 lucky 本体 mod 加载时显示
                if (ModItems.THE_LAST_END_LUCKY_BLOCK != null) {
                    output.accept(ModItems.THE_LAST_END_LUCKY_BLOCK.get());

                    ItemStack luckyVariant = new ItemStack(ModItems.THE_LAST_END_LUCKY_BLOCK.get());
                    luckyVariant.getOrCreateTag().putInt("Luck", 100);
                    output.accept(luckyVariant);

                    ItemStack unluckyVariant = new ItemStack(ModItems.THE_LAST_END_LUCKY_BLOCK.get());
                    unluckyVariant.getOrCreateTag().putInt("Luck", -100);
                    output.accept(unluckyVariant);
                }

                //剧情笔记
                output.accept(ModItems.MAGE_NOTE_1.get());
                output.accept(ModItems.MAGE_NOTE_2.get());
                output.accept(ModItems.MAGE_NOTE_3.get());
                output.accept(ModItems.MAGE_NOTE_4.get());
                output.accept(ModItems.DRAGON_CULT_SECRET_LETTER.get());
                output.accept(ModItems.TRAVELER_MESSAGE.get());
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
