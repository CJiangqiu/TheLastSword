package net.the_last_sword.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.init.ModSounds;
import net.the_last_sword.network.DangerousSkillPreviewPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;

import java.util.List;

/**
 * Base goal for dangerous skills. It owns their shared startup behavior while
 * subclasses implement skill-specific execution and cleanup.
 */
public abstract class DangerousSkillGoal<T extends TheLastEndEntity> extends Goal {
    protected final T dangerousEntity;
    private Vec3 dangerousSkillOrigin = Vec3.ZERO;
    private Vec3 dangerousSkillForward = new Vec3(0.0, 0.0, 1.0);
    private final double rangeWidth;
    private final double rangeLength;
    private final double rangeHeight;
    private final int previewEndFrame;
    private int skillFrame;
    private boolean previewActive;

    /**
     * Defines the shared targeting volume and its warning duration.
     * @param dangerousEntity skill owner
     * @param width full horizontal width in blocks
     * @param length forward length in blocks
     * @param height height above the locked origin in blocks
     * @param previewEndFrame zero-based skill frame at which the warning ends
     */
    protected DangerousSkillGoal(T dangerousEntity, double width, double length, double height,
                                 int previewEndFrame) {
        this.dangerousEntity = dangerousEntity;
        this.rangeWidth = width;
        this.rangeLength = length;
        this.rangeHeight = height;
        this.previewEndFrame = previewEndFrame;
    }

    @Override
    public final void start() {
        onDangerousSkillStart();
        if (!dangerousEntity.level().isClientSide) {
            dangerousSkillOrigin = dangerousEntity.position();
            dangerousSkillForward = Vec3.directionFromRotation(0.0F, dangerousEntity.getYRot());
            skillFrame = 0;
            previewActive = previewEndFrame > 0;
            if (previewActive) {
                syncPreview(previewEndFrame);
            }
            if (TheLastSwordConfiguration.getEntityDangerousSkillAlarmEnabledSafely()) {
                dangerousEntity.level().playSound(null, dangerousEntity.blockPosition(),
                    ModSounds.ALARM.get(), dangerousEntity.getSoundSource(), 0.5F, 1.0F);
            }
        }
    }

    @Override
    public final boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public final void tick() {
        if (!dangerousEntity.level().isClientSide && previewActive) {
            if (skillFrame >= previewEndFrame) {
                clearPreview();
            } else if (skillFrame > 0 && skillFrame % 5 == 0) {
                // 补发剩余预警，让中途开始追踪实体的玩家也能看到范围。
                syncPreview(previewEndFrame - skillFrame);
            }
        }
        tickDangerousSkill();
        skillFrame++;
    }

    @Override
    public final void stop() {
        clearPreview();
        onDangerousSkillStop();
    }

    // 判定与预警共用构造参数，避免范围分别维护产生偏差。
    protected final List<LivingEntity> getDangerousSkillTargets() {
        return EntityUtil.getTargetsInFrontBox(dangerousEntity, dangerousSkillOrigin,
                dangerousSkillForward, rangeWidth, rangeLength, rangeHeight);
    }

    private void syncPreview(int remainingTicks) {
        NetworkHandler.sendToTrackingClients(new DangerousSkillPreviewPacket(
                dangerousEntity.level().dimension().location(), dangerousEntity.getId(),
                dangerousEntity.getUUID(), dangerousSkillOrigin, dangerousSkillForward,
                rangeWidth, rangeLength, rangeHeight,
                dangerousEntity.level().getGameTime() + remainingTicks, remainingTicks > 0), dangerousEntity);
    }

    private void clearPreview() {
        if (previewActive && !dangerousEntity.level().isClientSide) {
            syncPreview(0);
        }
        previewActive = false;
    }

    protected final Vec3 getDangerousSkillOrigin() {
        return dangerousSkillOrigin;
    }

    protected final Vec3 getDangerousSkillForward() {
        return dangerousSkillForward;
    }

    protected abstract void onDangerousSkillStart();

    protected abstract void tickDangerousSkill();

    protected abstract void onDangerousSkillStop();
}
