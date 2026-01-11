package net.the_last_sword.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.gui.DragonCrystalEnchantingTableScreen;
import net.the_last_sword.client.gui.DragonCrystalSmithingTableScreen;
import net.the_last_sword.client.gui.SummonWraithGuiScreen;

//菜单屏幕注册
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreens {

    //注册菜单屏幕
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.DRAGON_CRYSTAL_SMITHING_TABLE.get(), DragonCrystalSmithingTableScreen::new);
            MenuScreens.register(ModMenus.DRAGON_CRYSTAL_ENCHANTING_TABLE.get(), DragonCrystalEnchantingTableScreen::new);
            MenuScreens.register(ModMenus.SUMMON_WRAITH_GUI.get(), SummonWraithGuiScreen::new);
        });
    }
}
