package net.the_last_sword;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.the_last_sword.configuration.TheLastSwordConfigManager;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.init.ModBlockEntities;
import net.the_last_sword.init.ModBlocks;
import net.the_last_sword.init.ModCreativeTabs;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.init.ModMenus;
import net.the_last_sword.init.ModRecipes;
import net.the_last_sword.init.ModSounds;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.TheLastSwordLogger;
import net.the_last_sword.event.ClientEventHandler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.PriorityQueue;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Mod(TheLastSwordMod.MOD_ID)
public class TheLastSwordMod {
    public static final String MOD_ID = "the_last_sword";

    //静态代码块：在类加载时立即设置 Agent 自附着权限（使用 Unsafe 底层方法）
    static {
        try {
            allowAttachSelfViaUnsafe();
            System.out.println("[TheLastSword] Agent self-attach enabled via Unsafe");
        } catch (Throwable t) {
            //降级到系统属性方法
            System.setProperty("jdk.attach.allowAttachSelf", "true");
            System.out.println("[TheLastSword] Unsafe method failed, fallback to system property: " + t.getMessage());
        }
    }

    //服务器任务调度系统
    private static final PriorityQueue<ScheduledTask> workQueue = new PriorityQueue<>();
    private static final Object queueLock = new Object();
    private static long currentServerTick = 0;

    //计划任务记录
    private record ScheduledTask(Runnable action, long executionTick) implements Comparable<ScheduledTask> {
        @Override
        public int compareTo(ScheduledTask other) {
            return Long.compare(this.executionTick, other.executionTick);
        }
    }

    public TheLastSwordMod() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModAttributes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        //注册网络包
        NetworkHandler.register();

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                TheLastSwordConfiguration.SPEC,
                "TheLastSword-common.toml"
        );

        TheLastSwordConfigManager.initializeConfig();

        boolean agentOpened = ensureOpened();
        TheLastSwordLogger.info("Agent opened: {}", agentOpened);
        TheLastSwordLogger.info("Agent log file: logs/TheLastSwordAgent.log");
    }

    //使用 Unsafe 直接修改 HotSpotVirtualMachine.ALLOW_ATTACH_SELF 字段
    private static void allowAttachSelfViaUnsafe() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);

        Class<?> vmClass = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
        java.lang.reflect.Field allowAttachSelfField = vmClass.getDeclaredField("ALLOW_ATTACH_SELF");

        //使用 Unsafe.putObject 直接修改字段值
        java.lang.reflect.Method staticFieldBaseMethod = unsafeClass.getMethod("staticFieldBase", java.lang.reflect.Field.class);
        java.lang.reflect.Method staticFieldOffsetMethod = unsafeClass.getMethod("staticFieldOffset", java.lang.reflect.Field.class);
        java.lang.reflect.Method putObjectMethod = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);

        Object fieldBase = staticFieldBaseMethod.invoke(unsafe, allowAttachSelfField);
        long fieldOffset = (long) staticFieldOffsetMethod.invoke(unsafe, allowAttachSelfField);

        putObjectMethod.invoke(unsafe, fieldBase, fieldOffset, Boolean.TRUE);
    }

    private static synchronized boolean ensureOpened() {
        if (tryLookup()) {
            return true;
        }

        try {
            Path tmpDir = Files.createTempDirectory("tls-agent");
            Path agent  = extract("/net/the_last_sword/agent/agent.jar", tmpDir.resolve("agent.jar"));

            String pid = String.valueOf(ProcessHandle.current().pid());

            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            Object vm = vmClass.getMethod("attach", String.class).invoke(null, pid);

            try {
                vmClass.getMethod("loadAgent", String.class, String.class)
                    .invoke(vm, agent.toString(), TheLastSwordMod.class.getName());
            } finally {
                vmClass.getMethod("detach").invoke(vm);
            }

            return tryLookup();
        } catch (Throwable t) {
            TheLastSwordLogger.error("attach failed", t);
            return false;
        }
    }

    private static boolean tryLookup() {
        try {
            Class<?> hashMapClass = Class.forName("java.util.HashMap");
            Class<?> nodeArrayClass = Class.forName("[Ljava.util.HashMap$Node;");
            MethodHandles.privateLookupIn(hashMapClass, MethodHandles.lookup())
                    .findVarHandle(hashMapClass, "table", nodeArrayClass);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static Path extract(String resource, Path target) throws Exception {
        try (InputStream in = TheLastSwordMod.class.getResourceAsStream(resource)) {
            if (in == null) throw new FileNotFoundException(resource);
            Files.copy(in, target, REPLACE_EXISTING);
        }
        target.toFile().deleteOnExit();
        return target.toAbsolutePath();
    }

    //添加服务器任务到队列
    public static void queueServerWork(int delayTicks, Runnable action) {
        synchronized (queueLock) {
            workQueue.add(new ScheduledTask(action, currentServerTick + delayTicks));
        }
    }

    //服务器任务调度处理器
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerTaskHandler {
        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                synchronized (queueLock) {
                    currentServerTick++;

                    //执行所有到期任务
                    while (!workQueue.isEmpty() && workQueue.peek().executionTick() <= currentServerTick) {
                        ScheduledTask task = workQueue.poll();
                        try {
                            task.action().run();
                        } catch (Exception e) {
                            TheLastSwordLogger.error("Error executing scheduled task", e);
                        }
                    }
                }
            }
        }
    }
}
