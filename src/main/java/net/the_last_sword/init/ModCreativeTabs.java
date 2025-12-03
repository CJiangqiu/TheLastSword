package net.the_last_sword.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheLastSwordMod.MOD_ID);

    //最终之剑物品栏
    public static final RegistryObject<CreativeModeTab> THE_LAST_SWORD_TAB = CREATIVE_MODE_TABS.register("the_last_sword_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("item_group.the_last_sword.the_last_sword_tab"))
            .icon(() -> new ItemStack(ModItems.DRAGON_CRYSTAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.DRAGON_CRYSTAL.get());
                output.accept(ModItems.DRAGON_CRYSTAL_UPGRADE_TEMPLATE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SMITHING_TABLE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SWORD.get());
                output.accept(ModItems.DRAGON_SWORD.get());
                output.accept(ModItems.THE_LAST_SWORD.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_HELMET.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_CHESTPLATE.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_LEGGINGS.get());
                output.accept(ModItems.DRAGON_CRYSTAL_ARMOR_BOOTS.get());
                output.accept(ModItems.DRAGON_ARMOR_HELMET.get());
                output.accept(ModItems.DRAGON_ARMOR_CHESTPLATE.get());
                output.accept(ModItems.DRAGON_ARMOR_LEGGINGS.get());
                output.accept(ModItems.DRAGON_ARMOR_BOOTS.get());
                output.accept(ModItems.DRAGON_CRYSTAL_SOUL_STONE.get());
                output.accept(ModItems.SWORD_SOUL_STONE.get());
                output.accept(ModItems.DRAGON_SOUL_LANTERN.get());
                output.accept(ModItems.THE_LAST_END_SWORD_WRAITH_SPAWN_EGG.get());
                output.accept(ModItems.THE_LAST_END_SWORD_WRAITH_LEVEL_13_SPAWN_EGG.get());

                //测试物品
                output.accept(ModItems.ULTRA_TEST_SWORD.get());
                output.accept(ModItems.TEST_ENTITY_SPAWN_EGG.get());
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
