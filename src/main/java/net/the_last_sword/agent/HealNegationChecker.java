package net.the_last_sword.agent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

//轻量级禁疗检查器 - 用于 Agent 环境
public class HealNegationChecker {

    private static MethodHandle isHealNegatedHandle = null;
    private static MethodHandle getHealNegationHealthHandle = null;
    private static boolean initialized = false;

    //初始化方法句柄（延迟初始化，避免类加载问题）
    private static void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            Class<?> managerClass = Class.forName("net.the_last_sword.attack.AttackManager");
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            //isHealNegated(Entity) -> boolean
            isHealNegatedHandle = lookup.findStatic(
                managerClass,
                "isHealNegated",
                MethodType.methodType(boolean.class, Entity.class)
            );

            //getHealNegationHealth(Entity) -> float
            getHealNegationHealthHandle = lookup.findStatic(
                managerClass,
                "getHealNegationHealth",
                MethodType.methodType(float.class, Entity.class)
            );

        } catch (Throwable t) {
            //静默失败，如果找不到类就返回默认值
            isHealNegatedHandle = null;
            getHealNegationHealthHandle = null;
        }
    }

    //检查实体是否被禁疗
    public static boolean isHealNegated(LivingEntity entity) {
        if (entity == null) return false;

        initialize();

        if (isHealNegatedHandle == null) {
            return false;
        }

        try {
            return (boolean) isHealNegatedHandle.invoke(entity);
        } catch (Throwable t) {
            return false;
        }
    }

    //获取禁疗值
    public static float getHealNegatedValue(LivingEntity entity) {
        if (entity == null) return 0.0F;

        initialize();

        if (getHealNegationHealthHandle == null) {
            return 0.0F;
        }

        try {
            return (float) getHealNegationHealthHandle.invoke(entity);
        } catch (Throwable t) {
            return 0.0F;
        }
    }

    //处理 getHealth 返回值：检查禁疗 + 调用钩子
    public static float processGetHealth(float originalHealth, LivingEntity entity, String className) {
        if (entity == null) return originalHealth;

        //检查是否禁疗
        if (isHealNegated(entity)) {
            return getHealNegatedValue(entity);
        }

        //调用钩子（用于字节码分析）
        try {
            Class<?> hookClass = Class.forName("net.the_last_sword.util.health.HealthGetterHook");
            MethodHandle hookHandle = MethodHandles.lookup().findStatic(
                hookClass,
                "onGetHealthCalled",
                MethodType.methodType(void.class, LivingEntity.class, String.class)
            );
            hookHandle.invoke(entity, className);
        } catch (Throwable t) {
            //静默失败，钩子不是必需的
        }

        //返回原始血量
        return originalHealth;
    }
}
