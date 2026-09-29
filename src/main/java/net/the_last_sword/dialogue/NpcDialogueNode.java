package net.the_last_sword.dialogue;

import java.util.List;

public record NpcDialogueNode(String id, String textKey, List<NpcDialogueOption> options) {

    public NpcDialogueNode {
        options = List.copyOf(options);
    }
}
