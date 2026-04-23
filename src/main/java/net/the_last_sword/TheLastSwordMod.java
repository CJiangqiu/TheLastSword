package net.the_last_sword;

import net.eca.api.EcaAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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
import net.the_last_sword.compat.apotheosis.ApotheosisCompat;

import java.util.PriorityQueue;

@Mod(TheLastSwordMod.MOD_ID)
public class TheLastSwordMod {
    public static final String MOD_ID = "the_last_sword";

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

        //注册内置资源包
        modEventBus.addListener(this::addPackFinders);

        //神化mod（Apotheosis）兼容：通过IMC声明剑类类别
        modEventBus.register(new ApotheosisCompat());

        //注册网络包
        NetworkHandler.register();

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                TheLastSwordConfiguration.SPEC,
                "TheLastSword-common.toml"
        );

        TheLastSwordConfigManager.initializeConfig();

        //初始化 ECA API 黑名单 - 保护最终之剑的实体数据字段
        initializeEcaProtection();
    }

    //初始化 ECA API 保护机制
    private void initializeEcaProtection() {
        try {
            //添加黑名单关键词，防止 ECA 的阶段2扫描修改这些字段
            EcaAPI.addHealthBlacklistKeyword("WORLD_ANCHOR");
            EcaAPI.addHealthBlacklistKeyword("WORLD_ANCHOR_MAX");
            EcaAPI.addHealthBlacklistKeyword("HEAL_BAN_TIME");
            EcaAPI.addHealthBlacklistKeyword("IS_PROTECTED");

            TheLastSwordLogger.info("ECA API protection initialized - TLS entity data fields are now protected");
        } catch (Exception e) {
            TheLastSwordLogger.error("Failed to initialize ECA API protection", e);
        }
    }

    //注册内置资源包
    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            var resourcePath = ModList.get().getModFileById(MOD_ID).getFile()
                    .findResource("resourcepacks", "The Last Sword Classical Texture Pack");
            event.addRepositorySource(consumer -> {
                var pack = Pack.readMetaAndCreate(
                        MOD_ID + ":classical_texture",
                        Component.literal("The Last Sword Classical Texture Pack"),
                        false,
                        path -> new PathPackResources(path, resourcePath, false),
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN
                );
                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        }
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
