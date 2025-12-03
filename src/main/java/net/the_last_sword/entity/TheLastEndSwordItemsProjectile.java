package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.util.EntityUtil;

import java.util.UUID;

/**
 * 最终之剑系列弹射物的抽象基类
 * 统一处理：友军检查、基础伤害、无敌时间清除、击中地面移除
 */
public abstract class TheLastEndSwordItemsProjectile extends ThrowableProjectile implements ItemSupplier {
    protected final UUID shooterUUID;
    protected boolean hasHitGround = false;

    public TheLastEndSwordItemsProjectile(EntityType<? extends TheLastEndSwordItemsProjectile> type, Level world) {
        super(type, world);
        this.shooterUUID = null;
    }

    public TheLastEndSwordItemsProjectile(EntityType<? extends TheLastEndSwordItemsProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
        super(type, entity, world);
        this.shooterUUID = shooterUUID;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * 击中实体时的统一处理流程
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();

        //检查射手是否存在
        Entity shooter = this.getOwner();
        if (shooter == null) {
            return;
        }

        //检查是否可以攻击目标
        if (!EntityUtil.canAttack(shooter, target)) {
            return;
        }

        if (target instanceof LivingEntity livingTarget) {
            //1. 造成基础物理伤害（由子类实现具体伤害值和类型）
            applyBaseDamage(livingTarget);

            //2. 清除无敌时间，让额外伤害能生效
            livingTarget.invulnerableTime = 0;

            //3. 造成额外伤害（由子类实现）
            applyExtraDamage(livingTarget);

            //4. 生成视觉效果（由子类实现）
            applyVisualEffect(livingTarget);
        } else if (target instanceof EndCrystal endCrystal) {
            //末影水晶特殊处理：直接掉落物品，不爆炸
            if (!endCrystal.level().isClientSide) {
                //生成末影水晶物品掉落
                ItemStack crystalItem = new ItemStack(Items.END_CRYSTAL);
                ItemEntity itemEntity = new ItemEntity(
                    endCrystal.level(),
                    endCrystal.getX(),
                    endCrystal.getY(),
                    endCrystal.getZ(),
                    crystalItem
                );
                endCrystal.level().addFreshEntity(itemEntity);
                //移除末影水晶实体
                endCrystal.discard();
            }
        }
    }

    /**
     * 造成基础物理伤害
     */
    protected abstract void applyBaseDamage(LivingEntity target);

    /**
     * 造成额外伤害
     */
    protected abstract void applyExtraDamage(LivingEntity target);

    /**
     * 生成视觉效果
     */
    protected abstract void applyVisualEffect(LivingEntity target);

    /**
     * 击中方块时：生成视觉效果，然后标记已击中地面
     */
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        //生成闪电视觉效果
        if (this.level() instanceof ServerLevel serverLevel) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                BlockPos pos = blockHitResult.getBlockPos();
                lightning.moveTo(Vec3.atBottomCenterOf(pos));
                lightning.setVisualOnly(true);
                serverLevel.addFreshEntity(lightning);
            }
        }
        this.hasHitGround = true;
    }

    /**
     * 每tick检查是否已击中地面，如果是则移除弹射物
     */
    @Override
    public void tick() {
        super.tick();
        if (this.hasHitGround) {
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return 0.03f;
    }
}
