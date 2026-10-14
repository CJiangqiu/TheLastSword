package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.network.BlinkRiftPacket;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, value = Dist.CLIENT)
public final class BlinkRiftRenderer {
    private static final List<Rift> RIFTS = new ArrayList<>();
    private static final Map<UUID, Rift> ACTORS = new HashMap<>();
    private static ClientLevel effectLevel;

    private BlinkRiftRenderer() {}

    public static void receive(BlinkRiftPacket packet) {
        update();
        if (effectLevel == null || !effectLevel.dimension().location().equals(packet.dimension())) return;
        if (packet.phase() == 3) {
            ACTORS.remove(packet.caster());
            return;
        }
        if (packet.phase() == 2) {
            ACTORS.remove(packet.caster());
            RIFTS.removeIf(rift -> rift.packet().caster().equals(packet.caster()));
            return;
        }
        if (RIFTS.size() >= 128) RIFTS.remove(0);
        Rift rift = new Rift(packet, effectLevel.getGameTime());
        RIFTS.add(rift);
        ACTORS.put(packet.caster(), rift);
    }

    private static void update() {
        ClientLevel level = Minecraft.getInstance().level;
        if (effectLevel != level) {
            RIFTS.clear();
            ACTORS.clear();
            effectLevel = level;
        }
        if (level != null) {
            RIFTS.removeIf(rift -> level.getGameTime() - rift.start() >= 50);
            ACTORS.values().removeIf(rift -> level.getGameTime() - rift.start() >= 50);
        }
    }

    private static boolean hidden(UUID caster) {
        update();
        Rift rift = ACTORS.get(caster);
        if (rift == null || effectLevel == null) return false;
        long age = effectLevel.getGameTime() - rift.start();
        return rift.packet().phase() == 0 ? age >= 15 : true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) update();
    }

    @SubscribeEvent
    public static void player(RenderPlayerEvent.Pre event) {
        if (hidden(event.getEntity().getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && hidden(player.getUUID())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        update();
        if (effectLevel == null || RIFTS.isEmpty()) return;
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        for (Rift rift : RIFTS) {
            float age = effectLevel.getGameTime() - rift.start() + event.getPartialTick();
            Vec3 offset = rift.packet().position().subtract(camera);
            pose.pushPose();
            pose.translate(offset.x, offset.y, offset.z);
            QueenBlinkRenderUtil.rift(pose, buffers, age, rift.packet().yaw());
            pose.popPose();
        }
        QueenBlinkRenderUtil.finishRiftBatch(buffers);
    }

    private record Rift(BlinkRiftPacket packet, long start) {}
}
