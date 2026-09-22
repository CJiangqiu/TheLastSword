package net.the_last_sword.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.DragonArmorEnergyStatus;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.item.DragonArmorItem;

import java.util.Locale;

//龙之盔甲全套 HUD 叠加层
public class DragonArmorOverlay {

    public static final int ENERGY_DISPLAY_BASE_Y = 30;

    //叠加层材质
    public static final ResourceLocation OVERLAY_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/screens/dragon_armor_overlay.png");

    //在所有原版 HUD 渲染之后渲染叠加层
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        //在 HOTBAR 渲染之后渲染，确保在最上层
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        //检查是否穿戴全套龙之盔甲
        if (!DragonArmorItem.isFullSet(player)) return;

        DefenceConfigData.PerceptionModule perception = DefenceConfig.getPerceptionModule();
        if (!perception.enableHud
                && !perception.showArmorEnergy
                && !perception.showEnergyConsumption) return;

        if (perception.enableHud) {
            renderOverlay(event.getGuiGraphics());
        }
        if (perception.showArmorEnergy || perception.showEnergyConsumption) {
            renderEnergyStatus(event.getGuiGraphics(), mc);
        }
    }

    //渲染龙之盔甲叠加层
    private static void renderOverlay(GuiGraphics gui) {
        int screenWidth = gui.guiWidth();
        int screenHeight = gui.guiHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //绘制全屏叠加层
        gui.blit(OVERLAY_TEXTURE, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderEnergyStatus(GuiGraphics gui, Minecraft mc) {
        DefenceConfigData.PerceptionModule perception = DefenceConfig.getPerceptionModule();
        DefenceConfigData.HudOffset offset = DefenceConfig.getHudOffset("dragon_armor_energy_display");
        int y = ENERGY_DISPLAY_BASE_Y + offset.y;

        if (perception.showArmorEnergy) {
            String percentage = String.format(Locale.ROOT, "%.1f",
                    DragonArmorEnergyStatus.getEnergyPercentage());
            Component text = Component.translatable(
                    "gui.the_last_sword.perception_hud.current_energy", percentage);
            gui.drawString(mc.font,
                    text,
                    gui.guiWidth() / 2 - mc.font.width(text) / 2 + offset.x,
                    y, 0xFFFFFF, true);
            y += mc.font.lineHeight + 2;
        }

        if (perception.showEnergyConsumption) {
            Component text = Component.translatable(
                    "gui.the_last_sword.perception_hud.total_consumption",
                    DragonArmorEnergyStatus.getConsumptionPerTick());
            gui.drawString(mc.font,
                    text,
                    gui.guiWidth() / 2 - mc.font.width(text) / 2 + offset.x,
                    y, 0xFFFFFF, true);
        }
    }
}
