package net.the_last_sword.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.UUID;

//最终之剑弹射物 - 持续AOE伤害+末地传送门闪电特效
@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class TheLastEndSwordProjectile extends AbstractArrow implements ItemSupplier {

    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.END_CRYSTAL);

    private final UUID shooterUUID;

    //最大存活时间：12秒，约240 ticks
    private static final int MAX_TICKS = 240;

    public TheLastEndSwordProjectile(EntityType<? extends TheLastEndSwordProjectile> type, Level world) {
        super(type, world);
        this.shooterUUID = null;
    }

    public TheLastEndSwordProjectile(EntityType<? extends TheLastEndSwordProjectile> type, LivingEntity entity, Level world, UUID shooterUUID) {
        super(type, entity, world);
        this.shooterUUID = shooterUUID;
    }

    public TheLastEndSwordProjectile(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), world);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return PROJECTILE_ITEM;
    }

    //保持无重力，飞行过程中不受重力影响
    @Override
    public boolean isNoGravity() {
        return true;
    }

    //每个tick执行：
    //1. 如果不扎地，则对以自身为中心半径2.5的立方体内的实体造成伤害（每tick都触发）
    //2. 客户端每tick在该立方体边缘生成粒子，显示实际范围
    //3. 如果扎地，则直接消失
    //4. 当tickCount达到MAX_TICKS（约12秒）后自动消失
    @Override
    public void tick() {
        super.tick();

        //1. AOE伤害逻辑（每tick检测，以立方体形式）
        if (!this.inGround) {
            double radius = 2.5; //半径2.5，构成一个边长为5的立方体
            AABB damageBox = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );
            for (Entity e : this.level().getEntities(this, damageBox, ent -> ent.isAlive() && ent instanceof LivingEntity)) {
                //排除发射者和友方
                if (e == this.getOwner()) continue;
                if (!EntityUtil.canAttack(this.getOwner(), e)) continue;
                if (e instanceof LivingEntity livingEntity) {
                    doProjectileDamage(livingEntity);
                }
            }
        }

        //2. 客户端粒子效果：在立方体所有6个面上每tick生成粒子
        if (this.level().isClientSide() && !this.inGround) {
            double cx = this.getX();
            double cy = this.getY();
            double cz = this.getZ();
            double r = 2.5; //半径
            //x = cx + r
            double offsetY = (Math.random() * 2 * r) - r;
            double offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + r, cy + offsetY, cz + offsetZ, 0, 0, 0);
            //x = cx - r
            offsetY = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx - r, cy + offsetY, cz + offsetZ, 0, 0, 0);
            //y = cy + r
            double offsetX = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + r, cz + offsetZ, 0, 0, 0);
            //y = cy - r
            offsetX = (Math.random() * 2 * r) - r;
            offsetZ = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy - r, cz + offsetZ, 0, 0, 0);
            //z = cz + r
            offsetX = (Math.random() * 2 * r) - r;
            offsetY = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + offsetY, cz + r, 0, 0, 0);
            //z = cz - r
            offsetX = (Math.random() * 2 * r) - r;
            offsetY = (Math.random() * 2 * r) - r;
            this.level().addParticle(ParticleTypes.PORTAL, cx + offsetX, cy + offsetY, cz - r, 0, 0, 0);
        }

        //3. 如果扎地，则直接消失
        if (this.inGround) {
            this.discard();
            return;
        }

        //4. 飞行超过12秒后删除
        if (this.tickCount >= MAX_TICKS) {
            this.discard();
            return;
        }
    }

    //当投射物直接命中实体时，保留原有逻辑不变：
    //调用super.onHitEntity后，再额外伤害目标（调用doProjectileDamage）
    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        if (this.getOwner() == null) {
            return;
        }
        if (!EntityUtil.canAttack(this.getOwner(), target)) {
            return;
        }
        super.onHitEntity(entityHitResult);
        if (target instanceof LivingEntity livingTarget) {
            doProjectileDamage(livingTarget);
        }
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            TheLastEndLightingEntity lightning = new TheLastEndLightingEntity(ModEntities.THE_LAST_END_LIGHTING.get(), serverLevel);
            lightning.moveTo(Vec3.atBottomCenterOf(blockHitResult.getBlockPos()));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
    }

    //读取主手剑的等级，根据配置计算额外伤害，并生成末地传送门闪电特效
    private void doProjectileDamage(LivingEntity target) {
        if (this.getOwner() instanceof LivingEntity owner) {
            //获取主手和副手的ItemStack
            ItemStack mainHandItem = owner instanceof Player ? ((Player) owner).getMainHandItem() : ItemStack.EMPTY;
            ItemStack offHandItem = owner instanceof Player ? ((Player) owner).getOffhandItem() : ItemStack.EMPTY;

            //检查两只手的武器，仅当其中一只手持有TheLastEndSword时，才将其作为武器源
            final ItemStack weapon;
            if (mainHandItem.getItem() instanceof TheLastSword) {
                weapon = mainHandItem; //主手是TheLastEndSword
            } else if (offHandItem.getItem() instanceof TheLastSword) {
                weapon = offHandItem; //副手是TheLastEndSword
            } else {
                weapon = ItemStack.EMPTY; //两只手都没有TheLastEndSword
            }

            //如果有TheLastEndSword，则造成基础物理伤害和额外虚空伤害
            if (!weapon.isEmpty() && weapon.getItem() instanceof TheLastSword theLastSword) {
                //1. 先造成基础物理伤害（通用物理伤害类型）
                float basePhysicalDamage = theLastSword.getBasePhysicalDamage();
                if (basePhysicalDamage > 0) {
                    DamageSource physicalDamageSource = new DamageSource(
                        owner.getCommandSenderWorld().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.GENERIC),
                        this,
                        owner
                    );
                    target.hurt(physicalDamageSource, basePhysicalDamage);
                }

                //2. 再造成额外虚空伤害
                int level = ItemLevelHelper.getLevel(weapon);
                double increaseValue = TheLastSwordConfiguration.getIncreaseValueSafely();
                double increaseValueHighLevel = TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
                double extraDamage = (level < 6 ? increaseValue : increaseValueHighLevel) * level;
                if (extraDamage < 0) {
                    extraDamage = 0;
                }

                //弹射物造成虚空伤害
                target.hurt(new DamageSource(
                        owner.getCommandSenderWorld().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD), owner, owner),
                        (float) extraDamage);
            } else {
                //如果没有TheLastEndSword，则使用默认伤害逻辑
                target.hurt(target.damageSources().magic(), 10.0F);
            }
        }

        //生成末地传送门闪电特效
        if (this.level() instanceof ServerLevel serverLevel) {
            TheLastEndLightingEntity lightning = new TheLastEndLightingEntity(ModEntities.THE_LAST_END_LIGHTING.get(), serverLevel);
            lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(this.getX(), this.getY(), this.getZ())));
            lightning.setVisualOnly(true);
            serverLevel.addFreshEntity(lightning);
        }
    }

    //----------------- 发射器方法 -----------------

    public static TheLastEndSwordProjectile shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 2.5f, 1024, 3);
    }

    public static TheLastEndSwordProjectile shoot(Level world, LivingEntity shooter, RandomSource random, float power, double damage, int knockback) {
        //降低速度：原来使用power * 2，现在改为(power * 2) - 1
        float adjustedSpeed = (power * 2) - 1;
        TheLastEndSwordProjectile projectile = new TheLastEndSwordProjectile(ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), shooter, world, shooter.getUUID());
        projectile.shoot(shooter.getViewVector(1).x, shooter.getViewVector(1).y, shooter.getViewVector(1).z, adjustedSpeed, 0);
        projectile.setSilent(true);
        projectile.setCritArrow(true);
        projectile.setBaseDamage(200);
        projectile.setKnockback(knockback);
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
        projectile.setBaseDamage(200);
        projectile.setKnockback(2);
        projectile.setCritArrow(true);
        shooter.level().addFreshEntity(projectile);
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.ender_dragon.shoot")),
                SoundSource.PLAYERS, 1,
                1f / (RandomSource.create().nextFloat() * 0.5f + 1));
        return projectile;
    }
}
