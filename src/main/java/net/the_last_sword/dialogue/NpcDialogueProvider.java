package net.the_last_sword.dialogue;

import net.minecraft.world.entity.player.Player;

public interface NpcDialogueProvider {

    String getDialogueId();

    boolean canStartDialogue(Player player);
}
