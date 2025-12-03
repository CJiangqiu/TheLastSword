package net.the_last_sword.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TheLastSwordAgent {

    //目标方法名（SRG 混淆名）
    private static final String TARGET_METHOD_NAME = "m_21223_";
    private static final String LIVING_ENTITY_CLASS = "net.minecraft.world.entity.LivingEntity";

    public static void premain(String args, Instrumentation inst) { agentmain(args, inst); }

    public static void agentmain(String args, Instrumentation inst) {
        try {
            AgentLogWriter.log("INFO", "[Agent] Starting agent initialization...");

            //通过已加载的类查找目标模块
            Module targetModule = null;
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(args)) {
                    targetModule = clazz.getModule();
                    break;
                }
            }
            if (targetModule == null) {
                throw new IllegalStateException("Target class not loaded: " + args);
            }

            //打开 java.util.* 和 java.lang.*
            Module base = Object.class.getModule();
            for (String pkg : base.getPackages()) {
                if (pkg.startsWith("java.util") || pkg.startsWith("java.lang")) {
                    open(inst, base, pkg, targetModule);
                }
            }

            //注册 transformer（canRetransform = true 用于已加载类）
            ClassFileTransformer healthTransformer = new HealthGetterTransformer();
            inst.addTransformer(healthTransformer, true);
            AgentLogWriter.log("INFO", "[Agent] Registered HealthGetterTransformer");

            //查找 LivingEntity 类
            Class<?> livingEntityClass = null;
            for (Class<?> clazz : inst.getAllLoadedClasses()) {
                if (clazz.getName().equals(LIVING_ENTITY_CLASS)) {
                    livingEntityClass = clazz;
                    break;
                }
            }

            if (livingEntityClass == null) {
                AgentLogWriter.log("WARN", "[Agent] LivingEntity class not found!");
                return;
            }

            //扫描所有类，找出：
            //1. LivingEntity 本身
            //2. 所有 LivingEntity 的子类中重写了 getHealth() 的类
            Class<?>[] allClasses = inst.getAllLoadedClasses();
            List<Class<?>> targetClasses = new ArrayList<>();

            AgentLogWriter.log("INFO", "[Agent] Scanning " + allClasses.length + " loaded classes for LivingEntity subclasses...");

            for (Class<?> clazz : allClasses) {
                //跳过不可修改的类
                if (!inst.isModifiableClass(clazz)) continue;
                if (clazz.isInterface() || clazz.isArray() || clazz.isPrimitive()) continue;

                String name = clazz.getName();

                //跳过 JDK 类
                if (name.startsWith("java.") || name.startsWith("javax.") ||
                        name.startsWith("sun.") || name.startsWith("jdk.") ||
                        name.startsWith("com.sun.") || name.startsWith("org.objectweb.asm.") ||
                        name.startsWith("net.the_last_sword.")) {
                    continue;
                }

                //检查是否是 LivingEntity 或其子类
                if (!livingEntityClass.isAssignableFrom(clazz)) {
                    continue;
                }

                //检查这个类是否自己定义（重写）了 getHealth() 方法
                if (hasOverriddenGetHealth(clazz)) {
                    targetClasses.add(clazz);
                    AgentLogWriter.log("INFO", "[Agent] Found class with getHealth(): " + name);
                }
            }

            AgentLogWriter.log("INFO", "[Agent] Found " + targetClasses.size() + " classes that override getHealth()");

            //Retransform 所有目标类
            int successCount = 0;
            int failCount = 0;

            for (Class<?> clazz : targetClasses) {
                try {
                    inst.retransformClasses(clazz);
                    successCount++;
                } catch (Throwable t) {
                    failCount++;
                    AgentLogWriter.log("WARN", "[Agent] Failed to retransform: " + clazz.getName(), t);
                }
            }

            AgentLogWriter.log("INFO", "[Agent] Retransformation completed: " + successCount + " success, " + failCount + " failed");
            AgentLogWriter.log("INFO", "[Agent] Agent initialization completed successfully");

        } catch (Throwable t) {
            AgentLogWriter.log("ERROR", "[Agent] Agent initialization failed", t);
            throw new RuntimeException(t);
        }
    }

    //检查类是否自己定义（重写）了 getHealth() 方法
    private static boolean hasOverriddenGetHealth(Class<?> clazz) {
        try {
            //getDeclaredMethods() 只返回类自己声明的方法，不包括继承的
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(TARGET_METHOD_NAME) &&
                        method.getParameterCount() == 0 &&
                        method.getReturnType() == float.class) {
                    return true;
                }
            }
        } catch (Throwable t) {
            //某些类可能无法反射，忽略
        }
        return false;
    }

    private static void open(Instrumentation inst, Module module, String pkg, Module to) {
        inst.redefineModule(
                module,
                Collections.emptySet(),
                Collections.emptyMap(),
                Collections.singletonMap(pkg, Collections.singleton(to)),
                Collections.emptySet(),
                Collections.emptyMap()
        );
    }

    private TheLastSwordAgent() {}
}