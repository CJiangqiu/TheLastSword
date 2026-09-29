package net.the_last_sword.dialogue;

import org.jetbrains.annotations.Nullable;

public record NpcDialogueOption(String textKey, @Nullable String nextNodeId) {

    public boolean closesDialogue() {
        return nextNodeId == null;
    }
}
