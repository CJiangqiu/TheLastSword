package net.the_last_sword.dialogue;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class NpcDialogueRegistry {
    public static final String QUEEN_DIALOGUE_ID = "the_past_shadow_of_the_queen";
    private static final String QUEEN_INTRO_NODE = "intro";
    private static final Map<String, NpcDialogue> DIALOGUES = Map.of(
            QUEEN_DIALOGUE_ID,
            new NpcDialogue(QUEEN_DIALOGUE_ID, QUEEN_INTRO_NODE, Map.of(
                    QUEEN_INTRO_NODE,
                    new NpcDialogueNode(QUEEN_INTRO_NODE,
                            "dialogue.the_last_sword.queen.intro",
                            List.of(new NpcDialogueOption(
                                    "dialogue.the_last_sword.option.leave", null))))));

    private NpcDialogueRegistry() {
    }

    @Nullable
    public static NpcDialogue get(String dialogueId) {
        return DIALOGUES.get(dialogueId);
    }
}
