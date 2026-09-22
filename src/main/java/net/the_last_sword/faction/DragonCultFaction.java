package net.the_last_sword.faction;

import net.eca.api.RegisterFaction;
import net.eca.util.faction.FactionDefinition;
import net.eca.util.faction.FactionRelation;

// 拜龙教阵营，由 ECA 在 FMLLoadCompleteEvent 扫描注册
@RegisterFaction
public class DragonCultFaction extends FactionDefinition {

    public static final String ID = "the_last_sword:dragon_cult";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "faction.the_last_sword.dragon_cult";
    }

    @Override
    public int getColor() {
        return 0xFF7B2FBE;
    }

    // 对无阵营实体敌对
    @Override
    public FactionRelation getStaticDefaultRelation() {
        return FactionRelation.HOSTILE;
    }
}
