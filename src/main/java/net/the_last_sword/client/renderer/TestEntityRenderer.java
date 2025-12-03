package net.the_last_sword.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.layer.TestEntityTheLastEndLayer;
import net.the_last_sword.test.TestEntity;

//测试实体渲染器
public class TestEntityRenderer extends HumanoidMobRenderer<TestEntity, HumanoidModel<TestEntity>> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/entity/test_entity.png");

    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(
            context,
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
            0.5f  // 阴影大小
        );

        //添加护甲层
        this.addLayer(new HumanoidArmorLayer<>(
            this,
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()
        ));

        //添加 The Last End 发光层
        this.addLayer(new TestEntityTheLastEndLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(TestEntity entity) {
        return TEXTURE;
    }
}
