package net.the_last_sword.compat;

import net.minecraftforge.fml.ModList;

//模组兼容性检测工具类
public class CompatCheck {

    private static Boolean jeiLoaded = null;
    private static Boolean cataclysmLoaded = null;
    private static Boolean curiosLoaded = null;
    private static Boolean touhouLittleMaidLoaded = null;
    private static Boolean jadeLoaded = null;
    private static Boolean tlsuvLoaded = null;
    private static Boolean apotheosisLoaded = null;
    private static Boolean luckyBlockLoaded = null;
    private static Boolean jecLoaded = null;

    public static boolean isJeiLoaded() {
        if (jeiLoaded == null) {
            jeiLoaded = ModList.get().isLoaded("jei");
        }
        return jeiLoaded;
    }

    public static boolean isCataclysmLoaded() {
        if (cataclysmLoaded == null) {
            cataclysmLoaded = ModList.get().isLoaded("cataclysm");
        }
        return cataclysmLoaded;
    }

    public static boolean isCuriosLoaded() {
        if (curiosLoaded == null) {
            curiosLoaded = ModList.get().isLoaded("curios");
        }
        return curiosLoaded;
    }

    public static boolean isTouhouLittleMaidLoaded() {
        if (touhouLittleMaidLoaded == null) {
            touhouLittleMaidLoaded = ModList.get().isLoaded("touhou_little_maid");
        }
        return touhouLittleMaidLoaded;
    }

    public static boolean isJadeLoaded() {
        if (jadeLoaded == null) {
            jadeLoaded = ModList.get().isLoaded("jade");
        }
        return jadeLoaded;
    }

    //检测"最终之剑：虚空之下"附属mod
    public static boolean isTLSUVLoaded() {
        if (tlsuvLoaded == null) {
            tlsuvLoaded = ModList.get().isLoaded("tlsuv");
        }
        return tlsuvLoaded;
    }

    //检测神化mod（Apotheosis）
    public static boolean isApotheosisLoaded() {
        if (apotheosisLoaded == null) {
            apotheosisLoaded = ModList.get().isLoaded("apotheosis");
        }
        return apotheosisLoaded;
    }

    //检测幸运方块本体 mod (Alex Socha 的 Lucky Block)
    public static boolean isLuckyBlockLoaded() {
        if (luckyBlockLoaded == null) {
            luckyBlockLoaded = ModList.get().isLoaded("lucky");
        }
        return luckyBlockLoaded;
    }

    //检测 Just Enough Characters（拼音搜索增强）
    public static boolean isJecLoaded() {
        if (jecLoaded == null) {
            jecLoaded = ModList.get().isLoaded("jecharacters");
        }
        return jecLoaded;
    }

}
