package net.the_last_sword.summon;

import net.eca.util.faction.Faction;
import net.eca.util.faction.FactionManager;
import net.eca.util.faction.FactionMember;
import net.eca.util.faction.FactionRelation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

//剑灵阵营门面 - 每个主人一个动态阵营，主人为首领，剑灵为成员
public class WraithFaction {

    //阵营 id 前缀，后接主人 UUID
    private static final String FACTION_PREFIX = "the_last_sword:wraith_";

    //阵营显示色（终焉紫）
    private static final int FACTION_COLOR = 0xFF9B59D0;

    //玩家实体类型 id，离线设置首领时使用
    private static final String PLAYER_TYPE_ID = "minecraft:player";

    private WraithFaction() {}

    //由主人 UUID 生成阵营 id
    public static String factionIdOf(UUID ownerUuid) {
        return ownerUuid == null ? null : FACTION_PREFIX + ownerUuid;
    }

    //确保主人的剑灵阵营存在且以其为首领
    public static void ensureFaction(Player owner, Level level) {
        if (owner == null || level == null) {
            return;
        }
        ensureFaction(owner.getUUID(), owner.getGameProfile().getName(), level);
    }

    //离线版：无需玩家实体即可建营并设首领
    public static void ensureFaction(UUID ownerUuid, String displayName, Level level) {
        if (ownerUuid == null || level == null) {
            return;
        }

        String factionId = factionIdOf(ownerUuid);
        Faction faction = FactionManager.getFaction(factionId);
        if (faction == null) {
            FactionManager.registerFaction(
                new Faction(factionId, displayName, FACTION_COLOR, FactionRelation.HOSTILE), level);
        } else if (displayName != null && !displayName.equals(faction.getDisplayName())) {
            //主人改名或迁移后补名：同对象回写仅为落库，成员表不受影响
            faction.setDisplayName(displayName);
            FactionManager.registerFaction(faction, level);
        }

        //首领即主人，setLeader 会自动将其加入阵营
        if (!ownerUuid.equals(FactionManager.getLeaderUuid(factionId))) {
            FactionManager.setLeader(factionId, new FactionMember(ownerUuid, PLAYER_TYPE_ID, true), level);
        }
    }

    //将剑灵绑定到主人的阵营
    public static void bind(Player owner, UUID wraithUuid, EntityType<?> type, Level level) {
        if (owner == null || wraithUuid == null || level == null) {
            return;
        }
        ensureFaction(owner, level);
        bind(owner.getUUID(), wraithUuid, typeIdOf(type), level);
    }

    //离线版：按 UUID 绑定，阵营须已存在
    public static boolean bind(UUID ownerUuid, UUID wraithUuid, String typeId, Level level) {
        if (ownerUuid == null || wraithUuid == null || level == null) {
            return false;
        }
        return FactionManager.joinFaction(wraithUuid, typeId, false, factionIdOf(ownerUuid), level);
    }

    //判断 UUID 是否为剑灵（属于剑灵阵营且非首领）
    public static boolean isWraith(UUID uuid) {
        return getOwnerUuid(uuid) != null;
    }

    //查询剑灵的主人 UUID，非剑灵返回 null
    public static UUID getOwnerUuid(UUID wraithUuid) {
        if (wraithUuid == null) {
            return null;
        }

        String factionId = FactionManager.getFactionId(wraithUuid);
        if (factionId == null || !factionId.startsWith(FACTION_PREFIX)) {
            return null;
        }

        //首领是主人本人，不是剑灵
        UUID leader = FactionManager.getLeaderUuid(factionId);
        return wraithUuid.equals(leader) ? null : leader;
    }

    //查询主人的全部剑灵 UUID
    public static Set<UUID> getWraiths(UUID ownerUuid) {
        if (ownerUuid == null) {
            return Collections.emptySet();
        }

        Set<UUID> members = FactionManager.getMemberUuids(factionIdOf(ownerUuid));
        if (members.isEmpty()) {
            return Collections.emptySet();
        }

        //成员表含首领本人，需剔除
        Set<UUID> wraiths = new HashSet<>(members);
        wraiths.remove(ownerUuid);
        return wraiths;
    }

    //取实体类型的注册 id
    private static String typeIdOf(EntityType<?> type) {
        if (type == null) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key == null ? "" : key.toString();
    }
}
