package net.thelastsword.capability;

import net.minecraft.network.chat.Component;

public class ReturnExtraAttackDamage {
    public static String execute() {
        return Component.translatable("item_tooltip.the_last_sword.extra_attack_damage").getString();
    }
}
