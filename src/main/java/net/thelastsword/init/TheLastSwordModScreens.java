
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.thelastsword.init;

import net.thelastsword.client.gui.DragonCrystalSmithingTableGuiScreen;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TheLastSwordModScreens {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(TheLastSwordModMenus.DRAGON_CRYSTAL_SMITHING_TABLE_GUI.get(), DragonCrystalSmithingTableGuiScreen::new);
		});
	}
}
