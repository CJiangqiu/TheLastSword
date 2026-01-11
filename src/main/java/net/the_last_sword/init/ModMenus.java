package net.the_last_sword.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;
import net.the_last_sword.client.gui.menu.DragonCrystalSmithingTableMenu;
import net.the_last_sword.client.gui.menu.SummonWraithGuiMenu;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<MenuType<DragonCrystalSmithingTableMenu>> DRAGON_CRYSTAL_SMITHING_TABLE =
        MENUS.register("dragon_crystal_smithing_table",
            () -> IForgeMenuType.create(DragonCrystalSmithingTableMenu::new)
        );

    public static final RegistryObject<MenuType<DragonCrystalEnchantingTableMenu>> DRAGON_CRYSTAL_ENCHANTING_TABLE =
        MENUS.register("dragon_crystal_enchanting_table",
            () -> IForgeMenuType.create(DragonCrystalEnchantingTableMenu::new)
        );

    public static final RegistryObject<MenuType<SummonWraithGuiMenu>> SUMMON_WRAITH_GUI =
        MENUS.register("summon_wraith_gui",
            () -> IForgeMenuType.create(SummonWraithGuiMenu::new)
        );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
