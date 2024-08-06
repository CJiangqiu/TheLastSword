package net.thelastsword.capability;

import net.minecraft.network.chat.Component;

public class ReturnSwordLevel {
    public static String execute() {
        return Component.translatable("item_tooltip.the_last_sword.dragon_sword").getString();
    }
}
