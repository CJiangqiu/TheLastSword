package net.the_last_sword.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.NpcDialogueStatePacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcDialogueManager {
    private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0;
    private static final String READ_DATA_KEY = "the_last_sword_npc_dialogue_read";
    private static final Map<ServerPlayer, DialogueSession> SESSIONS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private NpcDialogueManager() {
    }

    public static boolean open(ServerPlayer player, Entity entity) {
        if (player.distanceToSqr(entity) > MAX_INTERACTION_DISTANCE_SQR
                || !(entity instanceof NpcDialogueProvider provider)
                || !provider.canStartDialogue(player)) {
            return false;
        }
        NpcDialogue dialogue = NpcDialogueRegistry.get(provider.getDialogueId());
        if (dialogue == null || dialogue.getNode(dialogue.startNodeId()) == null) {
            return false;
        }

        DialogueSession session = new DialogueSession(entity.getId(), entity.getUUID(),
                dialogue.id(), dialogue.startNodeId());
        SESSIONS.put(player, session);
        NetworkHandler.sendToPlayer(NpcDialogueStatePacket.open(
                entity.getId(), dialogue.id(), dialogue.startNodeId(),
                getReadOptionIds(player, dialogue.id())), player);
        return true;
    }

    public static void select(ServerPlayer player, int optionIndex) {
        DialogueSession session = SESSIONS.get(player);
        if (session == null) {
            return;
        }
        if (optionIndex < 0) {
            close(player);
            return;
        }

        Entity entity = player.level().getEntity(session.entityId());
        if (entity == null || !entity.getUUID().equals(session.entityUuid())
                || player.distanceToSqr(entity) > MAX_INTERACTION_DISTANCE_SQR
                || !(entity instanceof NpcDialogueProvider provider)
                || !provider.getDialogueId().equals(session.dialogueId())
                || !provider.canStartDialogue(player)) {
            close(player);
            return;
        }

        NpcDialogue dialogue = NpcDialogueRegistry.get(session.dialogueId());
        NpcDialogueNode node = dialogue == null ? null : dialogue.getNode(session.nodeId());
        if (node == null || optionIndex >= node.options().size()) {
            close(player);
            return;
        }

        NpcDialogueOption option = node.options().get(optionIndex);
        if (option.closesDialogue()) {
            close(player);
            return;
        }

        NpcDialogueNode nextNode = dialogue.getNode(option.nextNodeId());
        if (nextNode == null) {
            close(player);
            return;
        }
        boolean alreadyRead = getReadOptionIds(player, dialogue.id()).contains(option.id());
        if (!alreadyRead && option.action() != null) {
            executeAction(player, option.action());
        }
        if (option.tracksRead() && !alreadyRead) {
            markOptionRead(player, dialogue.id(), option.id());
        }
        DialogueSession nextSession = session.withNode(nextNode.id());
        SESSIONS.put(player, nextSession);
        NetworkHandler.sendToPlayer(NpcDialogueStatePacket.open(
                entity.getId(), dialogue.id(), nextNode.id(),
                getReadOptionIds(player, dialogue.id())), player);
    }

    private static void executeAction(ServerPlayer player, NpcDialogueAction action) {
        if (action == NpcDialogueAction.GIVE_BLACKSMITHS_PAIN_NOTE) {
            ItemStack note = new ItemStack(ModItems.BLACKSMITHS_PAIN_NOTE.get());
            if (!player.getInventory().add(note)) {
                player.drop(note, false);
            }
        }
    }

    private static Set<String> getReadOptionIds(ServerPlayer player, String dialogueId) {
        CompoundTag dialogueData = player.getPersistentData()
                .getCompound(READ_DATA_KEY)
                .getCompound(dialogueId);
        Set<String> readOptionIds = new HashSet<>();
        for (String optionId : dialogueData.getAllKeys()) {
            if (dialogueData.getBoolean(optionId)) {
                readOptionIds.add(optionId);
            }
        }
        return readOptionIds;
    }

    private static void markOptionRead(ServerPlayer player, String dialogueId, String optionId) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag readData = playerData.getCompound(READ_DATA_KEY);
        CompoundTag dialogueData = readData.getCompound(dialogueId);
        dialogueData.putBoolean(optionId, true);
        readData.put(dialogueId, dialogueData);
        playerData.put(READ_DATA_KEY, readData);
    }

    //对话阅读状态属于玩家进度，死亡重生和维度切换时必须保留
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(READ_DATA_KEY)) {
            event.getEntity().getPersistentData().put(READ_DATA_KEY,
                    event.getOriginal().getPersistentData().getCompound(READ_DATA_KEY).copy());
        }
    }

    private static void close(ServerPlayer player) {
        SESSIONS.remove(player);
        NetworkHandler.sendToPlayer(NpcDialogueStatePacket.close(), player);
    }

    private record DialogueSession(int entityId, UUID entityUuid, String dialogueId, String nodeId) {

        private DialogueSession withNode(String nextNodeId) {
            return new DialogueSession(entityId, entityUuid, dialogueId, nextNodeId);
        }
    }
}
