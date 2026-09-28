package net.the_last_sword.entity;

import net.eca.api.EcaAPI;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;

import java.util.UUID;

public class QueenSummonedProjectile extends TheLastEndSwordProjectile {
    public QueenSummonedProjectile(EntityType<? extends QueenSummonedProjectile> type, Level level) {
        super(type, level);
    }

    public QueenSummonedProjectile(EntityType<? extends QueenSummonedProjectile> type, LivingEntity owner,
                                    Level level, UUID ownerUuid) {
        super(type, owner, level, ownerUuid);
    }

    public QueenSummonedProjectile(PlayMessages.SpawnEntity packet, Level level) {
        this(ModEntities.QUEEN_SUMMONED_PROJECTILE.get(), level);
    }

    @Override
    protected double getAoeRadius() {
        return TheLastSwordConfiguration.getQueenSummonProjectilesAoeRadiusSafely();
    }

    @Override
    protected void applyExtraDamage(Entity target) {
        if (!(getOwner() instanceof LivingEntity owner) || snapshotExtraDamage <= 0) {
            return;
        }
        DamageSource source = AbsoluteDestructionDamageSource.absoluteDestruction(owner);
        if (target instanceof LivingEntity living) {
            if (EntityUtil.canAttack(owner, living)) {
                EcaAPI.hurt(living, source, snapshotExtraDamage);
            }
        } else {
            target.hurt(source, snapshotExtraDamage);
        }
    }
}
