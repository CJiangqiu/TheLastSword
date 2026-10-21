package net.the_last_sword.faction;

import net.eca.api.RegisterFaction;
import net.eca.api.EcaAPI;
import net.eca.util.faction.FactionDefinition;
import net.eca.util.faction.FactionRelation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.summon.WraithSummonManager;

// 拜龙教阵营，由 ECA 在 FMLLoadCompleteEvent 扫描注册
@RegisterFaction
public class DragonCultFaction extends FactionDefinition {

    public static final String ID = "the_last_sword:dragon_cult";

    // 村庄只把仍属于拜龙教的野生单位视为威胁，不按实体类型误判已收服的剑灵。
    public static boolean isWildMember(LivingEntity entity) {
        return entity instanceof Mob && entity.isAlive() && !entity.isRemoved()
                && !(entity instanceof TheLastEndEntity theLastEnd && theLastEnd.isNpc())
                && !(entity instanceof TamableAnimal tamable && tamable.isTame())
                && !WraithSummonManager.isWraith(entity)
                && ID.equals(EcaAPI.getEntityFaction(entity));
    }

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
