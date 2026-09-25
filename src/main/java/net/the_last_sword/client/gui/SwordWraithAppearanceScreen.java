package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.the_last_sword.entity.SwordWraithAppearance;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.SetWraithAppearancePacket;

public class SwordWraithAppearanceScreen extends Screen {
    private static final int FRAME_WIDTH = 120;
    private static final int FRAME_HEIGHT = 140;
    private final InteractionHand hand;
    private SwordWraithAppearance appearance;
    private TheLastEndSwordWraithEntity preview;
    private int frameLeft;
    private int frameTop;

    public SwordWraithAppearanceScreen(InteractionHand hand, SwordWraithAppearance appearance) {
        super(Component.translatable("gui.the_last_sword.sword_wraith_appearance.title"));
        this.hand = hand;
        this.appearance = appearance;
    }

    @Override
    protected void init() {
        frameLeft = (width - FRAME_WIDTH) / 2;
        frameTop = Math.max(30, (height - FRAME_HEIGHT) / 2 - 12);

        addRenderableWidget(Button.builder(Component.literal("<"), button -> select(appearance.previous()))
                .bounds(frameLeft - 30, frameTop + FRAME_HEIGHT / 2 - 10, 20, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> select(appearance.next()))
                .bounds(frameLeft + FRAME_WIDTH + 10, frameTop + FRAME_HEIGHT / 2 - 10, 20, 20)
                .build());

        rebuildPreview();
    }

    private void select(SwordWraithAppearance selected) {
        appearance = selected;
        rebuildPreview();
        NetworkHandler.sendToServer(new SetWraithAppearancePacket(hand, selected));
    }

    private void rebuildPreview() {
        if (minecraft == null || minecraft.level == null) {
            preview = null;
            return;
        }
        preview = ModEntities.THE_LAST_END_SWORD_WRAITH.get().create(minecraft.level);
        if (preview != null) {
            preview.setAppearance(appearance);
            preview.setAppearancePreview(true);
            preview.setAnimationState(TheLastEndEntity.STATE_IDLE);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (preview != null) {
            preview.tickCount++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, title, width / 2, frameTop - 20, 0xFFFFFF);
        guiGraphics.fill(frameLeft - 2, frameTop - 2, frameLeft + FRAME_WIDTH + 2,
                frameTop + FRAME_HEIGHT + 2, 0xFF6F5A8A);
        guiGraphics.fill(frameLeft, frameTop, frameLeft + FRAME_WIDTH,
                frameTop + FRAME_HEIGHT, 0xCC120F1A);

        if (preview != null) {
            guiGraphics.enableScissor(frameLeft + 1, frameTop + 1,
                    frameLeft + FRAME_WIDTH - 1, frameTop + FRAME_HEIGHT - 1);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics,
                    width / 2, frameTop + FRAME_HEIGHT - 8, 48,
                    width / 2.0F - mouseX, frameTop + FRAME_HEIGHT / 2.0F - mouseY, preview);
            guiGraphics.disableScissor();
        }

        guiGraphics.drawCenteredString(font, Component.translatable(appearance.getNameKey()),
                width / 2, frameTop + FRAME_HEIGHT + 10, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
