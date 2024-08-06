
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.thelastsword.init;

import net.thelastsword.world.inventory.DragonCrystalSmithingTableGuiMenu;
import net.thelastsword.TheLastSwordMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

public class TheLastSwordModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TheLastSwordMod.MODID);
	public static final RegistryObject<MenuType<DragonCrystalSmithingTableGuiMenu>> DRAGON_CRYSTAL_SMITHING_TABLE_GUI = REGISTRY.register("dragon_crystal_smithing_table_gui", () -> IForgeMenuType.create(DragonCrystalSmithingTableGuiMenu::new));
}
