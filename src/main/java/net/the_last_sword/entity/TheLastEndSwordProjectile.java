package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

//最终之剑弹射物 - 持续AOE绝毁伤害+末地传送门闪电特效
public class TheLastEndSwordProjectile extends TheLastEndSwordItemsProjectile {

    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.END_CRYSTAL);

    //最大存活时间：12秒，约240 ticks
    private static final int MAX_TICKS = 240;

    //拖尾位置历史（客户端）
    private static final int TRAIL_LENGTH = 20;
    private final Deque<Vec3> trailPositions = new ArrayDeque<>();

    public TheLastEndSwordProjectile(EntityType<? extends TheLastEndSwordProjectile> type, Level world) {
        super(type, world);
    }

    public TheLastEndSwordProjectile(EntityType<? extends TheLastEndSwordProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
        super(type, entity, world, shooterUUID);
    }

    public TheLastEndSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), world);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    //无重力
    @Override
    protected float getGravity() {
        return 0f;
    }

    //获取拖尾位置历史
    public Deque<Vec3> getTrailPositions() {
        return trailPositions;
    }

    @Override
    public void tick() {
        super.tick();

        //记录位置历史（客户端拖尾用）
        if (this.level().isClientSide() && !this.hasHitGround) {
            trailPositions.addFirst(this.position());
            while (trailPositions.size() > TRAIL_LENGTH) {
                trailPositions.removeLast();
            }
        }

        //AOE伤害逻辑（每tick检测，以立方体形式）
        if (!this.hasHitGround && !this.level().isClientSide()) {
            double radius = 2.5;
            AABB damageBox = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );
            for (Entity e : this.level().getEntities(this, damageBox, ent -> ent.isAlive() && ent instanceof LivingEntity)) {
                if (e == this.getOwner()) continue;
                if (!EntityUtil.canAttack(this.getOwner(), e)) continue;
                if (e instanceof LivingEntity livingEntity) {
                    applyBaseDamage(livingEntity);
                    livingEntity.invulnerableTime = 0;
                    applyExtraDamage(livingEntity);
                    applyVisualEffect(livingEntity);
                }
            }
        }

        //客户端粒子效果：在立方体所有6个面上每tick生成粒子
        if (this.level().isClientSide() && !this.hasHitGround) {
            double cx = this.getX();
            double cy = this.getY();
            double cz = this.getZ();
            double r = 2.5;
            double offsetY = (Math.random() * 2 * r) - r;
            double offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + r, cy + offsetY, cz + offsetZ, 0, 0, 0);
            offsetY = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx - r, cy + offsetY, cz + offsetZ, 0, 0, 0);
            double offsetX = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + r, cz + offsetZ, 0, 0, 0);
            offsetX = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy - r, cz + offsetZ, 0, 0, 0);
            offsetX = (Math.random() * 2 * r) - r;
            offsetY = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + offsetY, cz + r, 0, 0, 0);
            offsetX = (Math.random() * 2 * r) - r;
            offsetY = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + offsetY, cz - r, 0, 0, 0);
        }

        //飞行超过12秒后删除
        if (this.tickCount >= MAX_TICKS) {
            this.discard();
        }
    }

    //查找发射者手中的最终之剑
    private ItemStack findWeapon() {
        if (this.getOwner() instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof TheLastSword) return mainHand;
            ItemStack offHand = player.getOffhandItem();
            if (offHand.getItem() instanceof TheLastSword) return offHand;
        }
        return ItemStack.EMPTY;
    }

    //造成基础物理伤害
    @Override
    protected void applyBaseDamage(LivingEntity target) {
        if (this.getOwner() instanceof LivingEntity owner) {
            ItemStack weapon = findWeapon();
            if (!weapon.isEmpty() && weapon.getItem() instanceof TheLastSword theLastSword) {
                float basePhysicalDamage = theLastSword.getBasePhysicalDamage() + enchantBonusDamage;
                if (basePhysicalDamage > 0) {
                    DamageSource source = new DamageSource(
                        owner.getCommandSenderWorld().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.GENERIC),
                        this, owner
                    );
                    target.hurt(source, basePhysicalDamage);
                }
            }
        }
    }

    //造成绝毁伤害
    @Override
    protected void applyExtraDamage(LivingEntity target) {
        if (this.getOwner() instanceof LivingEntity owner) {
            ItemStack weapon = findWeapon();
            if (!weapon.isEmpty()) {
                int level = ItemLevelHelper.getLevel(weapon);
                double increaseValue = TheLastSwordConfiguration.getIncreaseValueSafely();
                double increaseValueHighLevel = TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
                double extraDamage = (level < 6 ? increaseValue : increaseValueHighLevel) * level;
                if (extraDamage > 0) {
                    AbsoluteDestructionDamageSource.applyAbsoluteDestruction(target, owner, weapon, (float) extraDamage);
                }
            } else {
                target.hurt(target.damageSources().magic(), 10.0F);
            }
        }
    }

    //生成末地传送门闪电特效
    @Override
    protected void applyVisualEffect(LivingEntity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            TheLastEndLightingEntity lightning = new TheLastEndLightingEntity(ModEntities.THE_LAST_END_LIGHTING.get(), serverLevel);
            lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
    }

    //击中方块时生成末地闪电
    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level() instanceof ServerLevel serverLevel) {
            TheLastEndLightingEntity lightning = new TheLastEndLightingEntity(ModEntities.THE_LAST_END_LIGHTING.get(), serverLevel);
            lightning.moveTo(Vec3.atBottomCenterOf(blockHitResult.getBlockPos()));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
        this.hasHitGround = true;
    }

    //----------------- 发射器方法 -----------------

    public static TheLastEndSwordProjectile shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 2.5f, 1024, 3);
    }

    public static TheLastEndSwordProjectile shoot(Level world, LivingEntity shooter, RandomSource random, float power, double damage, int knockback) {
        float adjustedSpeed = (power * 2) - 1;
        TheLastEndSwordProjectile projectile = new TheLastEndSwordProjectile(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), shooter, world, shooter.getUUID());
        projectile.shoot(shooter.getViewVector(1).x, shooter.getViewVector(1).y, shooter.getViewVector(1).z, adjustedSpeed, 0);
        projectile.setSilent(true);
        //应用武器附魔
        ItemStack weapon = shooter.getMainHandItem();
        projectile.applyWeaponEnchantments(weapon);
        world.addFreshEntity(projectile);
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.shoot")),
                SoundSource.PLAYERS, 1,
                1f / (random.nextFloat() * 0.5f + 1) + (power / 2));
        return projectile;
    }

    public static TheLastEndSwordProjectile shoot(LivingEntity shooter, LivingEntity target) {
        TheLastEndSwordProjectile projectile = new TheLastEndSwordProjectile(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), shooter, shooter.level(), shooter.getUUID());
        double dx = target.getX() - shooter.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - shooter.getZ();
        projectile.shoot(dx, dy - projectile.getY() + Math.hypot(dx, dz) * 0.2F, dz, 5f, 12.0F);
        projectile.setSilent(true);
        //应用武器附魔
        ItemStack weapon = shooter.getMainHandItem();
        projectile.applyWeaponEnchantments(weapon);
        shooter.level().addFreshEntity(projectile);
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.shoot")),
                SoundSource.PLAYERS, 1,
                1f / (RandomSource.create().nextFloat() * 0.5f + 1));
        return projectile;
    }
}
