package net.the_last_sword.dialogue;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class NpcDialogueRegistry {
    public static final String QUEEN_DIALOGUE_ID = "the_past_shadow_of_the_queen";
    private static final String QUEEN_INTRO_NODE = "intro";
    private static final String QUEEN_IDENTITY_NODE = "identity";
    private static final String QUEEN_GIVERS_PAIN_NODE = "givers_pain";
    private static final String QUEEN_LAST_SWORD_NODE = "last_sword";
    private static final Map<String, NpcDialogue> DIALOGUES = Map.of(
            QUEEN_DIALOGUE_ID,
            new NpcDialogue(QUEEN_DIALOGUE_ID, QUEEN_INTRO_NODE, Map.of(
                    QUEEN_INTRO_NODE,
                    new NpcDialogueNode(QUEEN_INTRO_NODE,
                            "dialogue.the_last_sword.queen.intro",
                            List.of(
                                    new NpcDialogueOption("identity",
                                            "dialogue.the_last_sword.queen.option.identity",
                                            QUEEN_IDENTITY_NODE, true),
                                    new NpcDialogueOption("givers_pain",
                                            "dialogue.the_last_sword.queen.option.givers_pain",
                                            QUEEN_GIVERS_PAIN_NODE, true),
                                    new NpcDialogueOption("last_sword",
                                            "dialogue.the_last_sword.queen.option.last_sword",
                                            QUEEN_LAST_SWORD_NODE, true),
                                    new NpcDialogueOption("leave",
                                            "dialogue.the_last_sword.option.leave", null, false))),
                    QUEEN_IDENTITY_NODE,
                    createTopicNode(QUEEN_IDENTITY_NODE, "dialogue.the_last_sword.queen.identity"),
                    QUEEN_GIVERS_PAIN_NODE,
                    new NpcDialogueNode(QUEEN_GIVERS_PAIN_NODE,
                            "dialogue.the_last_sword.queen.givers_pain",
                            List.of(new NpcDialogueOption("take_blacksmiths_pain",
                                    "dialogue.the_last_sword.queen.option.take_blacksmiths_pain",
                                    QUEEN_INTRO_NODE, true,
                                    NpcDialogueAction.GIVE_BLACKSMITHS_PAIN_NOTE))),
                    QUEEN_LAST_SWORD_NODE,
                    createTopicNode(QUEEN_LAST_SWORD_NODE, "dialogue.the_last_sword.queen.last_sword"))));

    private NpcDialogueRegistry() {
    }

    private static NpcDialogueNode createTopicNode(String nodeId, String textKey) {
        return new NpcDialogueNode(nodeId, textKey,
                List.of(new NpcDialogueOption("back", "dialogue.the_last_sword.option.back",
                        QUEEN_INTRO_NODE, false)));
    }

    @Nullable
    public static NpcDialogue get(String dialogueId) {
        return DIALOGUES.get(dialogueId);
    }
}
