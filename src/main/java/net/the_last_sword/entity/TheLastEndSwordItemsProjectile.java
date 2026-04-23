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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.util.EntityUtil;

import java.util.UUID;

//最终之剑系列弹射物的抽象基类
public abstract class TheLastEndSwordItemsProjectile extends ThrowableProjectile implements ItemSupplier {
    protected final UUID shooterUUID;
    protected boolean hasHitGround = false;

    //附魔加成字段
    protected float enchantBonusDamage = 0;
    protected int enchantKnockback = 0;
    protected boolean enchantFlame = false;

    //发射时伤害快照
    protected float snapshotBaseDamage = 0;
    protected float snapshotExtraDamage = 0;

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

    //读取武器上的远程附魔并应用到弹射物
    public void applyWeaponEnchantments(ItemStack weapon) {
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, weapon);
        if (power > 0) {
            this.enchantBonusDamage = (float) (power * 0.5 + 0.5);
        }
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, weapon);
        if (punch > 0) {
            this.enchantKnockback = punch;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, weapon) > 0) {
            this.enchantFlame = true;
        }
    }

    //击中实体时的统一处理流程
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();

        Entity shooter = this.getOwner();
        if (shooter == null) {
            return;
        }

        if (!EntityUtil.canAttack(shooter, target)) {
            return;
        }

        //末影水晶特殊处理：转换为掉落物，不造成爆炸
        if (target instanceof EndCrystal endCrystal) {
            if (!endCrystal.level().isClientSide) {
                ItemStack crystalItem = new ItemStack(Items.END_CRYSTAL);
                ItemEntity itemEntity = new ItemEntity(
                    endCrystal.level(),
                    endCrystal.getX(),
                    endCrystal.getY(),
                    endCrystal.getZ(),
                    crystalItem
                );
                endCrystal.level().addFreshEntity(itemEntity);
                endCrystal.discard();
            }
            return;
        }

        //1. 造成基础物理伤害
        applyBaseDamage(target);

        //2. 清除无敌时间（仅 LivingEntity 有此字段；末影龙部件等由其自身 hurt 转发处理）
        if (target instanceof LivingEntity livingTarget) {
            livingTarget.invulnerableTime = 0;
        }

        //3. 造成额外伤害
        applyExtraDamage(target);

        //4. 附魔效果：击退
        if (enchantKnockback > 0) {
            Vec3 knockbackDir = this.getDeltaMovement().normalize().scale(enchantKnockback * 0.6);
            if (knockbackDir.lengthSqr() > 0) {
                target.push(knockbackDir.x, 0.1, knockbackDir.z);
            }
        }

        //5. 附魔效果：火矢
        if (enchantFlame) {
            target.setSecondsOnFire(5);
        }

        //6. 生成视觉效果
        applyVisualEffect(target);
    }

    //设置发射时伤害快照
    public void setSnapshotDamage(float baseDamage, float extraDamage) {
        this.snapshotBaseDamage = baseDamage;
        this.snapshotExtraDamage = extraDamage;
    }

    //造成基础物理伤害（target 可能是 LivingEntity 或末影龙部件等 Entity 子类）
    protected abstract void applyBaseDamage(Entity target);

    //造成额外伤害
    protected abstract void applyExtraDamage(Entity target);

    //生成视觉效果
    protected abstract void applyVisualEffect(Entity target);

    //击中方块时：生成视觉效果，然后标记已击中地面
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
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
