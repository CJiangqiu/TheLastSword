package net.the_last_sword.dialogue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.network.NpcDialogueStatePacket;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class NpcDialogueManager {
    private static final double MAX_INTERACTION_DISTANCE_SQR = 64.0;
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
                entity.getId(), dialogue.id(), dialogue.startNodeId()), player);
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
        DialogueSession nextSession = session.withNode(nextNode.id());
        SESSIONS.put(player, nextSession);
        NetworkHandler.sendToPlayer(NpcDialogueStatePacket.open(
                entity.getId(), dialogue.id(), nextNode.id()), player);
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
