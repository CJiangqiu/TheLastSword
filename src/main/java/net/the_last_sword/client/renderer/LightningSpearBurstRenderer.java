package net.the_last_sword.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.renderer.LightningSpearRenderUtil.BurstGeometry;
import net.the_last_sword.network.LightningSpearBurstPacket;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, value = Dist.CLIENT)
public final class LightningSpearBurstRenderer {
    private static final int LIFETIME = 10;
    private static final int MAX_EFFECTS = 64;
    private static final List<Burst> BURSTS = new ArrayList<>();
    private static ClientLevel effectLevel;

    private LightningSpearBurstRenderer() {
    }

    public static void receive(LightningSpearBurstPacket packet) {
        update();
        if (effectLevel == null || !effectLevel.dimension().location().equals(packet.dimension())
                || !Float.isFinite(packet.radius()) || packet.radius() <= 0.0F) {
            return;
        }
        if (BURSTS.size() >= MAX_EFFECTS) {
            BURSTS.remove(0);
        }
        BURSTS.add(new Burst(packet.position(), effectLevel.getGameTime(),
                LightningSpearRenderUtil.createBurst(Math.min(packet.radius(), 32.0F), packet.seed())));
    }

    private static void update() {
        ClientLevel level = Minecraft.getInstance().level;
        if (effectLevel != level) {
            BURSTS.clear();
            effectLevel = level;
        }
        if (level != null) {
            BURSTS.removeIf(burst -> level.getGameTime() - burst.startTick() >= LIFETIME);
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            update();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        update();
        if (effectLevel == null || BURSTS.isEmpty()) {
            return;
        }
        PoseStack poses = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        for (Burst burst : BURSTS) {
            float age = (float) (effectLevel.getGameTime() - burst.startTick()) + event.getPartialTick();
            float expansion = 0.75F + 0.25F * Mth.clamp(age, 0.0F, 1.0F);
            float alpha = 1.0F - Mth.clamp((age - 3.0F) / (LIFETIME - 3.0F), 0.0F, 1.0F);
            Vec3 offset = burst.position().subtract(camera);
            poses.pushPose();
            poses.translate(offset.x, offset.y, offset.z);
            poses.scale(expansion, expansion, expansion);
            LightningSpearRenderUtil.renderBurst(poses, buffers, burst.geometry(), alpha);
            poses.popPose();
        }
        LightningSpearRenderUtil.finishBurstBatch(buffers);
    }

    private record Burst(Vec3 position, long startTick, BurstGeometry geometry) {
    }
}
