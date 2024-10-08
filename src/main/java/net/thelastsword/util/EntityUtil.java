package net.thelastsword.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.thelastsword.configuration.TheLastSwordConfiguration;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class EntityUtil {

    // 获取范围内的实体
    public static List<Entity> getEntitiesInRange(Level world, Player player, double range) {
        AABB attackRange = new AABB(player.getX() - range / 2, player.getY() - range / 2, player.getZ() - range / 2,
                player.getX() + range / 2, player.getY() + range / 2, player.getZ() + range / 2);

        return world.getEntities(player, attackRange, e -> !(e instanceof ItemEntity) && !(e instanceof ExperienceOrb));
    }

    // 获取攻击目标
    public static List<Entity> EntityGoal(Level world, Player player, double range) {
        List<Entity> entities = getEntitiesInRange(world, player, range);

        return entities.stream()
                .filter(EntityUtil::shouldAttack)
                .collect(Collectors.toList());
    }

    // 判断是否应该攻击
    private static boolean shouldAttack(Entity target) {
        if (target instanceof Player) {
            return TheLastSwordConfiguration.ATTACK_PLAYERS.get();
        } else if (target instanceof Villager) {
            return TheLastSwordConfiguration.ATTACK_VILLAGERS.get();
        } else if (target instanceof Animal) {
            return TheLastSwordConfiguration.ATTACK_ANIMALS.get();
        } else if (target instanceof TamableAnimal) {
            return TheLastSwordConfiguration.ATTACK_TAMED.get();
        } else if (target instanceof IronGolem) {
            return TheLastSwordConfiguration.ATTACK_GOLEMS.get();
        } else if (target instanceof NeutralMob) {
            return TheLastSwordConfiguration.ATTACK_NEUTRAL.get();
        }
        return true;
    }

    // 绝对伤害方法
    public static void absoluteHurt(LivingEntity entity, float amount) {
        float newHealth = entity.getHealth() - amount;
        absoluteSetHealth(entity, newHealth);
    }

    // 绝对设定生命值方法
    public static void absoluteSetHealth(LivingEntity entity, float value) {
        entity.setHealth(value);
        if (entity.getHealth() != value) {
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(value);
        }
    }

    // 绝对清除方法
    public static void absoluteRemove(Entity entity) {
        try {
            Field removalReasonField = Entity.class.getDeclaredField("removalReason");
            removalReasonField.setAccessible(true);
            removalReasonField.set(entity, Entity.RemovalReason.DISCARDED);

            entity.remove(Entity.RemovalReason.DISCARDED);
            entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            entity.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            entity.remove(Entity.RemovalReason.KILLED);
            entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);

            // 检查实体是否仍然存活，如果是则使用setRemoved方法
            if (entity.isAlive()) {
                entity.setRemoved(Entity.RemovalReason.DISCARDED);
                entity.setRemoved(Entity.RemovalReason.KILLED);
                entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
