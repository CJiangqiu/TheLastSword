package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.OpenPaperNotePacket;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

//剧情纸条基类：右键阅读，确认后收录进终焉卷轴「旅途见闻」
public class PaperNote extends Item {

    //玩家持久数据中已收集纸条ID列表的键
    private static final String COLLECTED_NOTES_KEY = "the_last_sword:collected_paper_notes";

    private final String nameKey;
    private final String tooltipKey;
    private final String contentKey;
    private final String guiContentKey;

    public PaperNote(String nameKey, String tooltipKey, String contentKey, String guiContentKey) {
        super(new Item.Properties().stacksTo(1).fireResistant());
        this.nameKey = nameKey;
        this.tooltipKey = tooltipKey;
        this.contentKey = contentKey;
        this.guiContentKey = guiContentKey;
    }

    //右键打开纸条阅读GUI
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            String noteId = BuiltInRegistries.ITEM.getKey(this).toString();
            NetworkHandler.sendToPlayer(new OpenPaperNotePacket(
                noteId,
                nameKey,
                guiContentKey,
                isCollected(serverPlayer, noteId)
            ), serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    //物品悬停提示
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
    }

    //========== 收集数据 ==========

    //将纸条登记进玩家收集列表
    public static void markCollected(ServerPlayer player, String noteId) {
        ListTag list = player.getPersistentData().getList(COLLECTED_NOTES_KEY, Tag.TAG_STRING);
        if (list.stream().noneMatch(tag -> tag.getAsString().equals(noteId))) {
            list.add(StringTag.valueOf(noteId));
            player.getPersistentData().put(COLLECTED_NOTES_KEY, list);
        }
    }

    //判断纸条是否已收集
    public static boolean isCollected(ServerPlayer player, String noteId) {
        ListTag list = player.getPersistentData().getList(COLLECTED_NOTES_KEY, Tag.TAG_STRING);
        return list.stream().anyMatch(tag -> tag.getAsString().equals(noteId));
    }

    //获取已收集纸条ID列表
    public static List<String> getCollectedNoteIds(ServerPlayer player) {
        ListTag list = player.getPersistentData().getList(COLLECTED_NOTES_KEY, Tag.TAG_STRING);
        List<String> ids = new ArrayList<>();
        for (Tag tag : list) {
            ids.add(tag.getAsString());
        }
        return ids;
    }

    //判断id是否为已注册的纸条物品
    public static boolean isValidNoteId(String noteId) {
        ResourceLocation id = ResourceLocation.tryParse(noteId);
        return id != null && BuiltInRegistries.ITEM.get(id) instanceof PaperNote;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getContentKey() {
        return contentKey;
    }
}
