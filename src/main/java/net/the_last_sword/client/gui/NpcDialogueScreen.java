package net.the_last_sword.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.dialogue.NpcDialogue;
import net.the_last_sword.dialogue.NpcDialogueNode;
import net.the_last_sword.dialogue.NpcDialogueOption;
import net.the_last_sword.dialogue.NpcDialogueRegistry;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.NpcDialogueChoicePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NpcDialogueScreen extends Screen {
    private static final ResourceLocation SCROLL_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheLastSwordMod.MOD_ID, "textures/screens/scroll_background.png");
    private static final int TEXTURE_WIDTH = 430;
    private static final int TEXTURE_HEIGHT = 242;
    private static final int SOURCE_SIDE_BORDER = 34;
    private static final int SOURCE_TOP_BORDER = 24;
    private static final int SOURCE_BOTTOM_BORDER = 30;
    private static final int DESTINATION_SIDE_BORDER = 28;
    private static final int DESTINATION_TOP_BORDER = 18;
    private static final int DESTINATION_BOTTOM_BORDER = 22;
    private static final int MAX_PANEL_WIDTH = 430;
    private static final int MAX_PANEL_HEIGHT = 180;
    private static final int LINE_HEIGHT = 10;
    private static final int OPTION_GAP = 5;
    private static final int SCROLL_STEP = 18;
    private static final int TEXT_COLOR = 0xFF3C2415;
    private static final int OPTION_COLOR = 0xFF5A2E20;
    private static final int OPTION_HOVER_COLOR = 0xFF8A3F2C;
    private static final int READ_OPTION_COLOR = 0xFF77716C;
    private static final int READ_OPTION_HOVER_COLOR = 0xFF99918B;
    private final int entityId;
    private final String dialogueId;
    private String nodeId;
    private Set<String> readOptionIds;
    private float scrollOffset;
    private int maxScroll;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;
    private int viewportHeight;
    private int scrollBarLeft;
    private boolean draggingScrollBar;
    private boolean choicePending;
    private List<OptionLayout> optionLayouts = List.of();

    public NpcDialogueScreen(int entityId, String dialogueId, String nodeId, Set<String> readOptionIds) {
        super(Component.translatable("gui.the_last_sword.npc_dialogue.title"));
        this.entityId = entityId;
        this.dialogueId = dialogueId;
        this.nodeId = nodeId;
        this.readOptionIds = Set.copyOf(readOptionIds);
    }

    public boolean matches(int nextEntityId, String nextDialogueId) {
        return entityId == nextEntityId && dialogueId.equals(nextDialogueId);
    }

    public void setNode(String nextNodeId, Set<String> nextReadOptionIds) {
        nodeId = nextNodeId;
        readOptionIds = Set.copyOf(nextReadOptionIds);
        scrollOffset = 0.0F;
        choicePending = false;
        rebuildLayout();
    }

    @Override
    protected void init() {
        rebuildLayout();
    }

    private void rebuildLayout() {
        panelWidth = Math.min(MAX_PANEL_WIDTH, Math.max(220, width - 20));
        panelHeight = Math.min(MAX_PANEL_HEIGHT, Math.max(116, height / 2));
        panelLeft = (width - panelWidth) / 2;
        panelTop = height - panelHeight - 8;
        contentLeft = panelLeft + DESTINATION_SIDE_BORDER + 8;
        contentTop = panelTop + DESTINATION_TOP_BORDER + 6;
        scrollBarLeft = panelLeft + panelWidth - DESTINATION_SIDE_BORDER - 5;
        contentWidth = Math.max(80, scrollBarLeft - contentLeft - 8);
        viewportHeight = panelHeight - DESTINATION_TOP_BORDER - DESTINATION_BOTTOM_BORDER - 12;

        NpcDialogueNode node = getCurrentNode();
        if (node == null) {
            optionLayouts = List.of();
            maxScroll = 0;
            return;
        }

        int cursorY = font.split(Component.translatable(node.textKey()), contentWidth).size() * LINE_HEIGHT + 10;
        List<OptionLayout> layouts = new ArrayList<>();
        for (int index = 0; index < node.options().size(); index++) {
            NpcDialogueOption option = node.options().get(index);
            List<FormattedCharSequence> lines = font.split(Component.translatable(option.textKey()),
                    contentWidth - 16);
            int optionHeight = Math.max(18, lines.size() * LINE_HEIGHT + 8);
            layouts.add(new OptionLayout(index, cursorY, optionHeight, lines));
            cursorY += optionHeight + OPTION_GAP;
        }
        optionLayouts = List.copyOf(layouts);
        maxScroll = Math.max(0, cursorY - OPTION_GAP - viewportHeight);
        scrollOffset = Mth.clamp(scrollOffset, 0.0F, maxScroll);
    }

    private NpcDialogueNode getCurrentNode() {
        NpcDialogue dialogue = NpcDialogueRegistry.get(dialogueId);
        return dialogue == null ? null : dialogue.getNode(nodeId);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderNpc(graphics, mouseX, mouseY);
        renderScrollBackground(graphics);

        NpcDialogueNode node = getCurrentNode();
        if (node == null) {
            graphics.drawCenteredString(font, Component.translatable("gui.the_last_sword.npc_dialogue.invalid"),
                    width / 2, panelTop + panelHeight / 2, TEXT_COLOR);
            return;
        }

        graphics.enableScissor(contentLeft, contentTop, contentLeft + contentWidth, contentTop + viewportHeight);
        int drawY = contentTop - Mth.floor(scrollOffset);
        for (FormattedCharSequence line : font.split(Component.translatable(node.textKey()), contentWidth)) {
            graphics.drawString(font, line, contentLeft, drawY, TEXT_COLOR, false);
            drawY += LINE_HEIGHT;
        }

        for (OptionLayout option : optionLayouts) {
            int optionY = contentTop + option.contentY() - Mth.floor(scrollOffset);
            NpcDialogueOption dialogueOption = node.options().get(option.index());
            boolean read = dialogueOption.tracksRead() && readOptionIds.contains(dialogueOption.id());
            boolean hovered = isInside(mouseX, mouseY, contentLeft, optionY, contentWidth, option.height())
                    && mouseY >= contentTop && mouseY < contentTop + viewportHeight;
            int backgroundColor = hovered ? 0x55A85A3A : 0x337B432D;
            graphics.fill(contentLeft, optionY, contentLeft + contentWidth, optionY + option.height(), backgroundColor);
            graphics.fill(contentLeft, optionY, contentLeft + 2, optionY + option.height(),
                    hovered ? OPTION_HOVER_COLOR : OPTION_COLOR);
            int lineY = optionY + 4;
            int optionColor = read
                    ? (hovered ? READ_OPTION_HOVER_COLOR : READ_OPTION_COLOR)
                    : (hovered ? OPTION_HOVER_COLOR : OPTION_COLOR);
            for (FormattedCharSequence line : option.lines()) {
                graphics.drawString(font, line, contentLeft + 8, lineY, optionColor, false);
                lineY += LINE_HEIGHT;
            }
        }
        graphics.disableScissor();
        renderScrollBar(graphics);
    }

    private void renderNpc(GuiGraphics graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        int modelX = Math.max(60, width / 4);
        int modelBottom = panelTop + 4;
        int availableHeight = Math.max(70, panelTop - 18);
        int scale = Mth.clamp(availableHeight / 4, 28, 62);
        graphics.enableScissor(8, 8, Math.max(9, width / 2), panelTop + 6);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, modelX, modelBottom, scale,
                modelX - mouseX, modelBottom - availableHeight / 2.0F - mouseY, livingEntity);
        graphics.disableScissor();
        graphics.drawCenteredString(font, livingEntity.getDisplayName(), modelX,
                Math.max(8, panelTop - availableHeight - 4), 0xFFFFFF);
    }

    private void renderScrollBackground(GuiGraphics graphics) {
        int centerSourceWidth = TEXTURE_WIDTH - SOURCE_SIDE_BORDER * 2;
        int centerSourceHeight = TEXTURE_HEIGHT - SOURCE_TOP_BORDER - SOURCE_BOTTOM_BORDER;
        int centerWidth = panelWidth - DESTINATION_SIDE_BORDER * 2;
        int centerHeight = panelHeight - DESTINATION_TOP_BORDER - DESTINATION_BOTTOM_BORDER;

        blitSlice(graphics, panelLeft, panelTop, DESTINATION_SIDE_BORDER, DESTINATION_TOP_BORDER,
                0, 0, SOURCE_SIDE_BORDER, SOURCE_TOP_BORDER);
        blitSlice(graphics, panelLeft + DESTINATION_SIDE_BORDER, panelTop,
                centerWidth, DESTINATION_TOP_BORDER, SOURCE_SIDE_BORDER, 0,
                centerSourceWidth, SOURCE_TOP_BORDER);
        blitSlice(graphics, panelLeft + panelWidth - DESTINATION_SIDE_BORDER, panelTop,
                DESTINATION_SIDE_BORDER, DESTINATION_TOP_BORDER,
                TEXTURE_WIDTH - SOURCE_SIDE_BORDER, 0, SOURCE_SIDE_BORDER, SOURCE_TOP_BORDER);
        blitSlice(graphics, panelLeft, panelTop + DESTINATION_TOP_BORDER,
                DESTINATION_SIDE_BORDER, centerHeight, 0, SOURCE_TOP_BORDER,
                SOURCE_SIDE_BORDER, centerSourceHeight);
        blitSlice(graphics, panelLeft + DESTINATION_SIDE_BORDER, panelTop + DESTINATION_TOP_BORDER,
                centerWidth, centerHeight, SOURCE_SIDE_BORDER, SOURCE_TOP_BORDER,
                centerSourceWidth, centerSourceHeight);
        blitSlice(graphics, panelLeft + panelWidth - DESTINATION_SIDE_BORDER,
                panelTop + DESTINATION_TOP_BORDER, DESTINATION_SIDE_BORDER, centerHeight,
                TEXTURE_WIDTH - SOURCE_SIDE_BORDER, SOURCE_TOP_BORDER,
                SOURCE_SIDE_BORDER, centerSourceHeight);
        blitSlice(graphics, panelLeft, panelTop + panelHeight - DESTINATION_BOTTOM_BORDER,
                DESTINATION_SIDE_BORDER, DESTINATION_BOTTOM_BORDER,
                0, TEXTURE_HEIGHT - SOURCE_BOTTOM_BORDER, SOURCE_SIDE_BORDER, SOURCE_BOTTOM_BORDER);
        blitSlice(graphics, panelLeft + DESTINATION_SIDE_BORDER,
                panelTop + panelHeight - DESTINATION_BOTTOM_BORDER, centerWidth, DESTINATION_BOTTOM_BORDER,
                SOURCE_SIDE_BORDER, TEXTURE_HEIGHT - SOURCE_BOTTOM_BORDER,
                centerSourceWidth, SOURCE_BOTTOM_BORDER);
        blitSlice(graphics, panelLeft + panelWidth - DESTINATION_SIDE_BORDER,
                panelTop + panelHeight - DESTINATION_BOTTOM_BORDER,
                DESTINATION_SIDE_BORDER, DESTINATION_BOTTOM_BORDER,
                TEXTURE_WIDTH - SOURCE_SIDE_BORDER, TEXTURE_HEIGHT - SOURCE_BOTTOM_BORDER,
                SOURCE_SIDE_BORDER, SOURCE_BOTTOM_BORDER);
    }

    private void blitSlice(GuiGraphics graphics, int x, int y, int width, int height,
                           int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        graphics.blit(SCROLL_TEXTURE, x, y, width, height, sourceX, sourceY,
                sourceWidth, sourceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void renderScrollBar(GuiGraphics graphics) {
        if (maxScroll <= 0) {
            return;
        }
        int trackTop = contentTop;
        int trackHeight = viewportHeight;
        int thumbHeight = Math.max(18, viewportHeight * viewportHeight / (viewportHeight + maxScroll));
        int thumbTravel = trackHeight - thumbHeight;
        int thumbTop = trackTop + Math.round(thumbTravel * scrollOffset / maxScroll);
        graphics.fill(scrollBarLeft, trackTop, scrollBarLeft + 3, trackTop + trackHeight, 0x55482B1E);
        graphics.fill(scrollBarLeft - 1, thumbTop, scrollBarLeft + 4, thumbTop + thumbHeight, 0xFF8A4B32);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll > 0
                && isInside(mouseX, mouseY, scrollBarLeft - 3, contentTop, 9, viewportHeight)) {
            draggingScrollBar = true;
            setScrollFromMouse(mouseY);
            return true;
        }
        if (button == 0 && !choicePending) {
            for (OptionLayout option : optionLayouts) {
                int optionY = contentTop + option.contentY() - Mth.floor(scrollOffset);
                if (mouseY >= contentTop && mouseY < contentTop + viewportHeight
                        && isInside(mouseX, mouseY, contentLeft, optionY, contentWidth, option.height())) {
                    choicePending = true;
                    NetworkHandler.sendToServer(new NpcDialogueChoicePacket(option.index()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScrollBar) {
            setScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollBar) {
            draggingScrollBar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isInside(mouseX, mouseY, panelLeft, panelTop, panelWidth, panelHeight) && maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (float) delta * SCROLL_STEP, 0.0F, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void setScrollFromMouse(double mouseY) {
        float ratio = (float) ((mouseY - contentTop) / viewportHeight);
        scrollOffset = Mth.clamp(ratio * maxScroll, 0.0F, maxScroll);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int areaWidth, int areaHeight) {
        return mouseX >= x && mouseX < x + areaWidth && mouseY >= y && mouseY < y + areaHeight;
    }

    @Override
    public void onClose() {
        NetworkHandler.sendToServer(new NpcDialogueChoicePacket(NpcDialogueChoicePacket.CLOSE_DIALOGUE));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record OptionLayout(int index, int contentY, int height, List<FormattedCharSequence> lines) {
    }
}
