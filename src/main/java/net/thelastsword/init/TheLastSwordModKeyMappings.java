package net.thelastsword.init;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.TheLastSwordMod;
import net.thelastsword.network.ChangeTheLastSwordModeMessage;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class TheLastSwordModKeyMappings {
    public static final KeyMapping CHANGE_SWORD_MODE = new KeyMapping("key.the_last_sword.change_sword_mode", GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.gameplay");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_SWORD_MODE);
    }

    @Mod.EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getAction() == GLFW.GLFW_RELEASE) { // 检测按键释放事件
                if (event.getKey() == CHANGE_SWORD_MODE.getKey().getValue()) {
                    TheLastSwordMod.PACKET_HANDLER.sendToServer(new ChangeTheLastSwordModeMessage());
                }
            }
        }
    }
}
