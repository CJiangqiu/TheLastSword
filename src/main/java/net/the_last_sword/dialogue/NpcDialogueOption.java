package net.the_last_sword.dialogue;

import org.jetbrains.annotations.Nullable;

public record NpcDialogueOption(String id, String textKey, @Nullable String nextNodeId,
                                boolean tracksRead, @Nullable NpcDialogueAction action) {

    public NpcDialogueOption(String id, String textKey, @Nullable String nextNodeId, boolean tracksRead) {
        this(id, textKey, nextNodeId, tracksRead, null);
    }

    public boolean closesDialogue() {
        return nextNodeId == null;
    }
}
