package net.the_last_sword.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.health.WorldAnchorManager;

//把不可恢复的生命区间覆盖到原有生命值槽位，避免增加额外 HUD 行
public final class WorldAnchorHealthOverlay {

    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(
            TheLastSwordMod.MOD_ID, "textures/screens/world_anchor_full.png");
    private static final int ICON_SIZE = 9;
    private static final int ICON_SPACING = 8;
    private static final int HEARTS_PER_ROW = 10;

    private WorldAnchorHealthOverlay() {
    }

    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())
                || !TheLastSwordConfiguration.getDisplayWorldAnchorDamageSafely()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isCreative() || player.isSpectator()) {
            return;
        }

        float worldAnchor = WorldAnchorManager.getSyncedWorldAnchor(player);
        if (worldAnchor < 0.0F) {
            return;
        }

        float maxHealth = player.getMaxHealth();
        if (!Float.isFinite(maxHealth) || maxHealth <= 0.0F || !Float.isFinite(worldAnchor)) {
            return;
        }

        int maxHealthPoints = Mth.ceil(maxHealth);
        int availableHealthPoints = Mth.clamp(Mth.ceil(worldAnchor), 0, maxHealthPoints);
        if (availableHealthPoints >= maxHealthPoints) {
            return;
        }

        renderLostWorldAnchor(
                event.getGuiGraphics(), minecraft, player, maxHealthPoints, availableHealthPoints);
    }

    private static void renderLostWorldAnchor(GuiGraphics guiGraphics, Minecraft minecraft, Player player,
                                               int maxHealthPoints, int availableHealthPoints) {
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((maxHealthPoints + absorption) / 20.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int consumedHeight = healthRows * rowHeight;
        if (rowHeight != 10) {
            consumedHeight += 10 - rowHeight;
        }

        int left = guiGraphics.guiWidth() / 2 - 91;
        int top = guiGraphics.guiHeight() - (((ForgeGui) minecraft.gui).leftHeight - consumedHeight);
        int heartCount = Mth.ceil(maxHealthPoints / 2.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int heartIndex = 0; heartIndex < heartCount; heartIndex++) {
            int leftPoint = heartIndex * 2;
            int rightPoint = leftPoint + 1;
            boolean leftBlocked = leftPoint < maxHealthPoints && leftPoint >= availableHealthPoints;
            boolean rightBlocked = rightPoint < maxHealthPoints && rightPoint >= availableHealthPoints;
            if (!leftBlocked && !rightBlocked) {
                continue;
            }

            int x = left + heartIndex % HEARTS_PER_ROW * ICON_SPACING;
            int y = top - heartIndex / HEARTS_PER_ROW * rowHeight;
            if (leftBlocked && rightBlocked) {
                guiGraphics.blit(ICON, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            } else if (leftBlocked) {
                int halfWidth = (ICON_SIZE + 1) / 2;
                guiGraphics.blit(ICON, x, y, 0, 0, halfWidth, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            } else {
                int sourceX = ICON_SIZE / 2;
                int halfWidth = ICON_SIZE - sourceX;
                guiGraphics.blit(ICON, x + sourceX, y, sourceX, 0,
                        halfWidth, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            }
        }
        RenderSystem.disableBlend();
    }
}
