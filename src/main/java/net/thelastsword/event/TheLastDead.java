package net.thelastsword.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.lang.reflect.Method;

public class TheLastDead {
    public TheLastDead() {
    }

    public static void theLastDead(Entity entity) {
        // Check if the world is server-side / 检查是否在服务器端
        if (!entity.getCommandSenderWorld().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) entity.getCommandSenderWorld();

            // Check if the entity is a LivingEntity / 检查实体是否为LivingEntity
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // 1. Try to drop loot / 尝试掉落实体掉落物
                try {
                    Method dropLoot = LivingEntity.class.getDeclaredMethod("dropFromLootTable", DamageSource.class, boolean.class);
                    dropLoot.setAccessible(true);
                    dropLoot.invoke(livingEntity, DamageTypes.GENERIC, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2. Deal damage equal to max health / 造成实体最大生命值的伤害
                float maxHealth = (float) livingEntity.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
                absoluteHurt(livingEntity, maxHealth);

                // 3. If entity is still alive, perform NBT operations / 如果实体仍然存活，进行NBT操作
                if (livingEntity.isAlive()) {
                    // 绝对清除实体
                    try {


                        livingEntity.remove(Entity.RemovalReason.DISCARDED);
                        livingEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
                        livingEntity.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                        livingEntity.remove(Entity.RemovalReason.KILLED);
                        livingEntity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);

                        // 检查实体是否仍然存活，如果是则使用setRemoved方法
                        if (livingEntity.isAlive()) {
                            livingEntity.setRemoved(Entity.RemovalReason.DISCARDED);
                            livingEntity.setRemoved(Entity.RemovalReason.KILLED);
                            livingEntity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                            livingEntity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
                            livingEntity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 清空实体数据，关闭实体AI，暂停实体的活动
                    CompoundTag nbtData = new CompoundTag();
                    nbtData.putBoolean("NoAI", true);
                    nbtData.putBoolean("Silent", true);
                    nbtData.putBoolean("Invulnerable", true);
                    nbtData.putBoolean("Invisible", true); // 设置不可见
                    livingEntity.load(nbtData);


                }
            } else {
                // Apply the same logic for non-LivingEntity / 对非LivingEntity对象应用相同逻辑
                // 1. Try to drop loot / 尝试掉落实体掉落物
                try {
                    Method dropLoot = Entity.class.getDeclaredMethod("dropFromLootTable", DamageSource.class, boolean.class);
                    dropLoot.setAccessible(true);
                    dropLoot.invoke(entity, DamageTypes.GENERIC, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 2. Deal damage equal to max health / 造成实体最大生命值的伤害
                absoluteHurt((LivingEntity) entity, 1024);

                // 3. If entity is still alive, perform NBT operations / 如果实体仍然存活，进行NBT操作
                if (entity.isAlive()) {
                    // 绝对清除实体
                    try {


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
                    // 清空实体数据，关闭实体AI，暂停实体的活动
                    CompoundTag nbtData = new CompoundTag();
                    nbtData.putBoolean("NoAI", true);
                    nbtData.putBoolean("Silent", true);
                    nbtData.putBoolean("Invulnerable", true);
                    nbtData.putBoolean("Invisible", true); // 设置不可见
                    entity.load(nbtData);


                }
            }
        }
    }

    // 绝对伤害方法
    private static void absoluteHurt(LivingEntity entity, float amount) {
        float newHealth = entity.getHealth() - amount;
        absoluteSetHealth(entity, newHealth);
    }

    // 绝对设定生命值方法
    private static void absoluteSetHealth(LivingEntity entity, float value) {
        entity.setHealth(value);
        if (entity.getHealth() != value) {
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(value);
        }
    }
}
