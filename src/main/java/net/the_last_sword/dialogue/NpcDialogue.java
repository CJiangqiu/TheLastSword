package net.the_last_sword.dialogue;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record NpcDialogue(String id, String startNodeId, Map<String, NpcDialogueNode> nodes) {

    public NpcDialogue {
        nodes = Map.copyOf(nodes);
    }

    @Nullable
    public NpcDialogueNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }
}
