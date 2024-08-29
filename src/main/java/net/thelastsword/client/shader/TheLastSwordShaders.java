package net.thelastsword.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = "the_last_sword", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TheLastSwordShaders {
    public static ShaderInstance cosmicShader;
    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicScale;
    public static Uniform cosmicOffsetX;
    public static Uniform cosmicOffsetY;

    public static int renderTime;
    public static float renderFrame;

    public static RenderType COSMIC_RENDER_TYPE = RenderType.create("the_last_sword:cosmic",
            DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(() -> cosmicShader))
                    .setDepthTestState(CustomRenderState.NO_DEPTH_TEST)
                    .setLightmapState(CustomRenderState.LIGHTMAP)
                    .setTransparencyState(CustomRenderState.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(CustomRenderState.BLOCK_SHEET)
                    .createCompositeState(true)
    );

    @SubscribeEvent
public static void onRegisterShaders(RegisterShadersEvent event) {
    try {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("the_last_sword", "the_last_sword"), DefaultVertexFormat.BLOCK), e -> {
            cosmicShader = e;
            cosmicTime = cosmicShader.getUniform("time");
            cosmicYaw = cosmicShader.getUniform("yaw");
            cosmicPitch = cosmicShader.getUniform("pitch");
            cosmicExternalScale = cosmicShader.getUniform("externalScale");
            cosmicOpacity = cosmicShader.getUniform("opacity");
            cosmicScale = cosmicShader.getUniform("scale");
            cosmicOffsetX = cosmicShader.getUniform("offsetX");
            cosmicOffsetY = cosmicShader.getUniform("offsetY");
            if (cosmicScale != null) cosmicScale.set(1.0f); // 设置默认值
            if (cosmicOffsetX != null) cosmicOffsetX.set(0.0f); // 设置默认值
            if (cosmicOffsetY != null) cosmicOffsetY.set(0.0f); // 设置默认值
        });
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}


    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.END) {
            ++renderTime;
        }
    }

    @SubscribeEvent
    public static void renderTick(TickEvent.RenderTickEvent event) {
        if (!Minecraft.getInstance().isPaused() && event.phase == TickEvent.Phase.START) {
            renderFrame = event.renderTickTime;
        }
    }

    public static class CustomRenderState extends RenderStateShard {
        public static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = new RenderStateShard.DepthTestStateShard("no_depth_test", 519);
        public static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
        public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }, RenderSystem::disableBlend);
        public static final RenderStateShard.TextureStateShard BLOCK_SHEET = new RenderStateShard.TextureStateShard(new ResourceLocation("textures/atlas/blocks.png"), false, false);

        public CustomRenderState(String name, Runnable setupTask, Runnable clearTask) {
            super(name, setupTask, clearTask);
        }
    }
}
