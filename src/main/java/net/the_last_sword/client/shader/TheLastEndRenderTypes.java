package net.the_last_sword.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

//The Last End 渲染类型工厂
//为不同的渲染场景（实体、物品、方块、粒子）创建对应的 RenderType
public class TheLastEndRenderTypes {

    //创建实体表面的 The Last End 效果渲染类型
    //适用于: 实体外表渲染（如 TestEntity）
    public static RenderType createEntityEffect(ResourceLocation texture) {
        return RenderType.create(
            "the_last_end_entity_effect",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用 The Last End 着色器
                .setShaderState(new RenderStateShard.ShaderStateShard(TheLastEndEffect::getShader))
                //深度测试
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                //光照贴图
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //透明度混合
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                //使用指定纹理
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                //禁用面剔除
                .setCullState(RenderStateShard.NO_CULL)
                //覆盖层
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true)
        );
    }

    //创建物品的 The Last End 效果渲染类型（使用纹理图集）
    //适用于: 手持物品、掉落物、GUI 中的物品
    public static RenderType createItemEffectWithAtlas() {
        return RenderType.create(
            "the_last_end_item_effect_atlas",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用 The Last End 着色器
                .setShaderState(new RenderStateShard.ShaderStateShard(TheLastEndEffect::getShader))
                //深度测试
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                //透明度混合
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                //使用物品纹理图集
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                //禁用面剔除
                .setCullState(RenderStateShard.NO_CULL)
                //覆盖层
                .setOverlayState(RenderStateShard.OVERLAY)
                //只写入颜色，不写入深度（避免透明区域遮挡）
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .createCompositeState(true)
        );
    }

    //创建物品的 The Last End 效果渲染类型（使用指定纹理）
    //适用于: 特殊物品渲染
    public static RenderType createItemEffect(ResourceLocation texture) {
        return RenderType.create(
            "the_last_end_item_effect",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用 The Last End 着色器
                .setShaderState(new RenderStateShard.ShaderStateShard(TheLastEndEffect::getShader))
                //深度测试
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                //透明度混合
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                //使用指定纹理
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                //禁用面剔除
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(true)
        );
    }

    //创建方块/天空盒的 The Last End 效果渲染类型
    //适用于: 方块特效、天空盒渲染
    public static RenderType createBlockEffect(ResourceLocation texture) {
        return RenderType.create(
            "the_last_end_block_effect",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用 The Last End 着色器
                .setShaderState(new RenderStateShard.ShaderStateShard(TheLastEndEffect::getShader))
                //深度测试
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                //光照贴图
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //透明度混合
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                //使用方块纹理集
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .createCompositeState(true)
        );
    }

    //创建粒子的 The Last End 效果渲染类型
    //适用于: 剑刃周围的星点粒子效果
    public static RenderType createParticleEffect() {
        return RenderType.create(
            "the_last_end_particle_effect",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用标准位置颜色纹理着色器
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                //加法混合 - 创造发光效果
                .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                //禁用深度写入 - 避免遮挡其他粒子
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                //禁用面剔除
                .setCullState(RenderStateShard.NO_CULL)
                //使用方块纹理集
                .setTextureState(RenderStateShard.BLOCK_SHEET)
                .createCompositeState(false)
        );
    }

    //创建发光层的 The Last End 效果渲染类型（不使用自定义着色器）
    //适用于: 发光边缘效果
    public static RenderType createGlowEffect(ResourceLocation texture) {
        return RenderType.create(
            "the_last_end_glow_effect",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            2097152,
            true,
            false,
            RenderType.CompositeState.builder()
                //使用标准实体着色器（带光照贴图）
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                //透明度混合
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                //禁用面剔除
                .setCullState(RenderStateShard.NO_CULL)
                //视图偏移 Z 分层 - 防止 Z-fighting
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                //使用指定纹理
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                //光照贴图
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //覆盖层
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true)
        );
    }

    private TheLastEndRenderTypes() {}
}
