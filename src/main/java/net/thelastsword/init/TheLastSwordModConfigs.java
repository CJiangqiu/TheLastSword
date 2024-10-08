package net.thelastsword.init;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.thelastsword.TheLastSwordMod;
import net.thelastsword.configuration.TheLastSwordConfiguration;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheLastSwordModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TheLastSwordConfiguration.SPEC, "The Last Sword.toml");
		});
	}
}
