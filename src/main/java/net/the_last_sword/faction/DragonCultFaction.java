package net.the_last_sword.faction;

import net.eca.api.RegisterFaction;
import net.eca.util.faction.FactionDefinition;
import net.eca.util.faction.FactionRelation;
import net.minecraft.world.entity.EntityType;
import net.the_last_sword.init.ModEntities;

import java.util.Map;

// 拜龙教阵营，由 ECA 在 FMLLoadCompleteEvent 扫描注册
@RegisterFaction
public class DragonCultFaction extends FactionDefinition {

    public static final String ID = "the_last_sword:dragon_cult";

    private Map<EntityType<?>, Integer> memberTypes;

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

    // 对无阵营实体敌对，维持原有的主动攻击行为
    @Override
    public FactionRelation getStaticDefaultRelation() {
        return FactionRelation.HOSTILE;
    }

    // 袭击波次按权重抽取的成员池
    @Override
    public Map<EntityType<?>, Integer> getMemberEntityTypes() {
        if (memberTypes == null) {
            memberTypes = Map.of(
                    ModEntities.DRAGON_CULTIST.get(), 4,
                    ModEntities.DRAGON_CULT_PALADIN.get(), 1
            );
        }
        return memberTypes;
    }
}
