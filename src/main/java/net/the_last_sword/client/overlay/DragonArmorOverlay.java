package net.the_last_sword.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.item.DragonArmorItem;

//龙之盔甲全套 HUD 叠加层
public class DragonArmorOverlay {

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

        //检查配置是否启用 HUD
        if (!DefenceConfig.isDragonArmorHudEnabled()) return;

        //渲染叠加层
        renderOverlay(event.getGuiGraphics(), mc);
    }

    //渲染龙之盔甲叠加层
    private static void renderOverlay(GuiGraphics gui, Minecraft mc) {
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
}
