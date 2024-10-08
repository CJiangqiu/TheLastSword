package net.thelastsword.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CosmicModelLoader implements IGeometryLoader<CosmicModelLoader.CosmicModelGeometry> {

    @Override
    public CosmicModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
        // 读取JSON数据并创建CosmicModelGeometry实例
        return new CosmicModelGeometry();
    }

    public static class CosmicModelGeometry implements IUnbakedGeometry<CosmicModelGeometry> {

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides, ResourceLocation modelLocation) {
            // 创建并返回自定义的BakedModel实例
            return new CosmicBakedModel();
        }
    }

    public static class CosmicBakedModel implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
            // 返回渲染所需的BakedQuad列表
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return true;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            // 返回粒子图标
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(new ResourceLocation("the_last_sword", "textures/item/the_last_sword_hilt.png"));
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}
