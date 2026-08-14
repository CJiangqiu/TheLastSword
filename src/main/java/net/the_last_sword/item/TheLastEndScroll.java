package net.the_last_sword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.OpenLastEndScrollPacket;
import net.the_last_sword.network.OpenLastEndScrollPacket.PaperNoteEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TheLastEndScroll extends Item {

    private static final String RUINED_VILLAGE_X_KEY = "RuinedVillageX";
    private static final String RUINED_VILLAGE_Z_KEY = "RuinedVillageZ";
    private static final int RUINED_VILLAGE_SEARCH_RADIUS = 256;
    private static final TagKey<Structure> RUINED_VILLAGE = TagKey.create(
        Registries.STRUCTURE,
        new ResourceLocation(TheLastSwordMod.MOD_ID, "ruined_village")
    );

    public TheLastEndScroll() {
        super(new Item.Properties()
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.EPIC)
        );
    }

    //右键打开教程书GUI
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = itemStack.getOrCreateTag();
            if (!tag.contains(RUINED_VILLAGE_X_KEY, Tag.TAG_INT)
                    || !tag.contains(RUINED_VILLAGE_Z_KEY, Tag.TAG_INT)) {
                BlockPos ruinedVillage = serverPlayer.serverLevel().findNearestMapStructure(
                    RUINED_VILLAGE,
                    serverPlayer.blockPosition(),
                    RUINED_VILLAGE_SEARCH_RADIUS,
                    false
                );
                if (ruinedVillage != null) {
                    tag.putInt(RUINED_VILLAGE_X_KEY, ruinedVillage.getX());
                    tag.putInt(RUINED_VILLAGE_Z_KEY, ruinedVillage.getZ());
                }
            }

            boolean hasLocation = tag.contains(RUINED_VILLAGE_X_KEY, Tag.TAG_INT)
                && tag.contains(RUINED_VILLAGE_Z_KEY, Tag.TAG_INT);
            NetworkHandler.sendToPlayer(new OpenLastEndScrollPacket(
                hasLocation,
                tag.getInt(RUINED_VILLAGE_X_KEY),
                tag.getInt(RUINED_VILLAGE_Z_KEY),
                collectPaperNotes(serverPlayer)
            ), serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    //从玩家收集数据组装已收集纸条列表
    private static List<PaperNoteEntry> collectPaperNotes(ServerPlayer player) {
        List<PaperNoteEntry> notes = new ArrayList<>();
        for (String noteId : PaperNote.getCollectedNoteIds(player)) {
            ResourceLocation id = ResourceLocation.tryParse(noteId);
            Item item = id == null ? null : BuiltInRegistries.ITEM.get(id);
            if (item instanceof PaperNote note) {
                notes.add(new PaperNoteEntry(note.getNameKey(), note.getContentKey()));
            }
        }
        return notes;
    }

    //添加物品描述
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_last_end_scroll")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
