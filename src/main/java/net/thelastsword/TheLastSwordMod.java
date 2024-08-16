package net.thelastsword;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.thelastsword.network.ChangeTheLastSwordModeMessage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.thelastsword.init.TheLastSwordModTabs;
import net.thelastsword.init.TheLastSwordModMenus;
import net.thelastsword.init.TheLastSwordModItems;
import net.thelastsword.init.TheLastSwordModEntities;
import net.thelastsword.init.TheLastSwordModBlocks;
import net.thelastsword.init.TheLastSwordModBlockEntities;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("the_last_sword")
public class TheLastSwordMod {
    public static final Logger LOGGER = LogManager.getLogger(TheLastSwordMod.class);
    public static final String MODID = "the_last_sword";
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public TheLastSwordMod() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        TheLastSwordModBlocks.REGISTRY.register(bus);
        TheLastSwordModBlockEntities.REGISTRY.register(bus);
        TheLastSwordModItems.REGISTRY.register(bus);
        TheLastSwordModEntities.REGISTRY.register(bus);

        TheLastSwordModTabs.REGISTRY.register(bus);

        TheLastSwordModMenus.REGISTRY.register(bus);

        init();
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MODID, MODID),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0)
                    actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }

    private void init() {
        addNetworkMessage(ChangeTheLastSwordModeMessage.class, ChangeTheLastSwordModeMessage::buffer, ChangeTheLastSwordModeMessage::new, ChangeTheLastSwordModeMessage::handler);
    }
}
