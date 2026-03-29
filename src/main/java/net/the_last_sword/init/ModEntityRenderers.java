package net.the_last_sword.init;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.model.WingsThatCoverTheWorldModel;
import net.the_last_sword.client.renderer.*;

//实体渲染器注册
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
    //注册实体渲染器
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_CRYSTAL_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON_LIGHTING.get(), DragonLightingRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_LIGHTING.get(), TheLastEndLightingRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.TEST_ENTITY.get(), TestEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_OF_SEALED_SPIRE.get(), GuardianOfSealedSpireRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_SABER.get(), GuardianOfSealedSpireRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_BERSERKER.get(), GuardianOfSealedSpireRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_ARCHER.get(), GuardianOfSealedSpireRenderer::new);
        event.registerEntityRenderer(ModEntities.LOST_WRAITH.get(), LostWraithRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_LAST_END_SWORD_WRAITH.get(), TheLastEndSwordWraithRenderer::new);
        event.registerEntityRenderer(ModEntities.THE_PAST_SHADOW_OF_THE_QUEEN.get(), ThePastShadowOfTheQueenRenderer::new);
    }

    //注册 Model Layer 定义
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WingsThatCoverTheWorldModel.LAYER_LOCATION, WingsThatCoverTheWorldModel::createBodyLayer);
    }

    //注册方块实体渲染器
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlockEntities.DRAGON_CRYSTAL_ENCHANTING_TABLE.get(),
                context -> new DragonCrystalEnchantingTableRenderer());
        });
    }
}
