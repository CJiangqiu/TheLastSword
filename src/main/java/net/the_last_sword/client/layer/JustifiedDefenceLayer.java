package net.the_last_sword.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.the_last_sword.configuration.DefenceConfig;
import net.the_last_sword.configuration.DefenceConfigData;
import net.the_last_sword.init.ModAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JustifiedDefenceLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final int HIT_FLASH_TICKS = 10;
    private static final Map<UUID, Double> previousShield = new HashMap<>();
    private static final Map<UUID, Long> hitTick = new HashMap<>();

    private final LivingEntityRenderer<T, M> renderer;

    public JustifiedDefenceLayer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        DefenceConfigData.ShieldEffectMode mode = DefenceConfig.getJustifiedDefence().overlayEffect;
        if (mode == DefenceConfigData.ShieldEffectMode.DISABLED) return;

        AttributeInstance attr = entity.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (attr == null) return;

        double shield = attr.getValue();
        long now = entity.level().getGameTime();
        UUID uuid = entity.getUUID();

        Double prev = previousShield.get(uuid);
        if (prev != null && shield < prev) {
            hitTick.put(uuid, now);
        }
        previousShield.put(uuid, shield);

        Long lastHit = hitTick.get(uuid);
        if (lastHit == null || now - lastHit > HIT_FLASH_TICKS) return;

        float elapsed = now - lastHit + partialTicks;
        float alpha = 0.30f * (1.0f - elapsed / HIT_FLASH_TICKS);
        if (alpha <= 0.0f) return;

        renderOverlay(poseStack, bufferSource, packedLight, alpha);
    }

    private void renderOverlay(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float alpha) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lightning());
        this.renderer.getModel().renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, alpha);
    }
}
