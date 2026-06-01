package net.the_last_sword.compat.jec;

import me.towdium.jecharacters.utils.Match;
import net.minecraftforge.fml.ModList;

public class JECCompat {

    private static final boolean LOADED = ModList.get().isLoaded("jecharacters");

    // 支持拼音匹配，未安装JEC时退回普通包含判断
    public static boolean contains(String text, String query) {
        if (LOADED) {
            return Match.contains(text, query);
        }
        return text.toLowerCase().contains(query.toLowerCase());
    }
}
