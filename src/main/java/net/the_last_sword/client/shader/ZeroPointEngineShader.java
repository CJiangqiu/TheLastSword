package net.the_last_sword.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.eca.client.render.shader.EcaShaderInstance;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.the_last_sword.TheLastSwordMod;

import java.io.IOException;

public final class ZeroPointEngineShader extends EcaShaderInstance {
    private static ShaderInstance shader;
    private final Uniform localUvMin;
    private final Uniform localUvScale;

    public static final RenderType ITEM = RenderType.create(
            "zero_point_engine_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
            256, false, false, RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> shader))
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));

    public ZeroPointEngineShader(ResourceProvider provider) throws IOException {
        super(provider, ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "zero_point_engine"),
                DefaultVertexFormat.NEW_ENTITY);
        localUvMin = getUniform("LocalUvMin");
        localUvScale = getUniform("LocalUvScale");
    }

    public static void setShader(ShaderInstance instance) {
        shader = instance;
    }

    @Override
    public void apply() {
        // 图集中的位置会在资源重载后变化，始终使用当前绘制批次的局部坐标。
        applyLocalUvBoundsUniforms(localUvMin, localUvScale);
        super.apply();
    }
}
