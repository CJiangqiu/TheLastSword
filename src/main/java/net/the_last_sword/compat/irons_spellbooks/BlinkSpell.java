package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.QueenBlinkSettings;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.QueenBlinkBlade;
import net.the_last_sword.network.BlinkRiftPacket;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.ParticleUtil;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlinkSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "blink");
    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.COMMON)
        .setSchoolResource(ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end"))
        .setMaxLevel(10)
        .setCooldownSeconds(12)
        .build();
    // 冷却接口没有等级参数，使用调用范围内的等级以隔离不同玩家。
    private final ThreadLocal<Integer> cooldownLevel = new ThreadLocal<>();
    private final Set<UUID> activeBlinks = new HashSet<>();
    private static final int RIFT_OPEN_TICKS = 15;

    public BlinkSpell() {
        baseManaCost = 30;
        manaCostPerLevel = 10;
        castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_ID;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.the_last_sword.blink.distance", getDistance(spellLevel)),
            Component.translatable("ui.the_last_sword.blink.damage", 20));
    }

    private static int getDistance(int spellLevel) {
        return 12 + Math.max(0, spellLevel - 1) * 2;
    }

    @Override
    public int getSpellCooldown() {
        Integer spellLevel = cooldownLevel.get();
        return Math.max(0, super.getSpellCooldown() - (spellLevel == null ? 0 : Math.max(0, spellLevel - 1) * 20));
    }

    public int getEffectiveCooldown(int spellLevel, Player player, CastSource source) {
        Integer previousLevel = cooldownLevel.get();
        cooldownLevel.set(spellLevel);
        try {
            return MagicManager.getEffectiveSpellCooldown(this, player, source);
        } finally {
            restoreCooldownLevel(previousLevel);
        }
    }

    private void restoreCooldownLevel(Integer previousLevel) {
        if (previousLevel == null) {
            cooldownLevel.remove();
        } else {
            cooldownLevel.set(previousLevel);
        }
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData magicData) {
        return checkDestination(level, spellLevel, caster);
    }

    private boolean checkDestination(Level level, int spellLevel, LivingEntity caster) {
        if (activeBlinks.contains(caster.getUUID())) return false;
        Destination result = inspectDestination(level, caster, getDistance(spellLevel));
        if (result.position() != null) {
            return true;
        }
        if (caster instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(result.tooFar()
                ? "ui.the_last_sword.blink.too_far" : "ui.the_last_sword.blink.no_destination",
                getDistance(spellLevel)), true);
        }
        return false;
    }

    @Override
    public void castSpell(Level level, int spellLevel, ServerPlayer player, CastSource source,
                          boolean triggerCooldown) {
        // 流程结束前禁止再次扣费和启动第二组裂缝。
        if (!checkDestination(level, spellLevel, player)) {
            return;
        }
        Integer previousLevel = cooldownLevel.get();
        cooldownLevel.set(spellLevel);
        try {
            super.castSpell(level, spellLevel, player, source, triggerCooldown);
        } finally {
            restoreCooldownLevel(previousLevel);
        }
    }

    private static void sendRift(LivingEntity caster, Vec3 position, float yaw, int phase) {
        NetworkHandler.sendToTrackingClientsAndSelf(new BlinkRiftPacket(caster.level().dimension().location(),
            caster.getUUID(), position, yaw, phase), caster);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
        if (!level.isClientSide) {
            if (cooldownLevel.get() != null) {
                cooldownLevel.set(spellLevel);
            }
            Vec3 destination = inspectDestination(level, caster, getDistance(spellLevel)).position();
            if (destination != null && activeBlinks.add(caster.getUUID())) {
                float yaw = caster.getYRot();
                sendRift(caster, caster.position(), yaw, 0);
                queuePhase(level, caster, destination, yaw, false, level.getGameTime() + RIFT_OPEN_TICKS);
            }
        }
        super.onCast(level, spellLevel, caster, source, magicData);
    }

    private void queuePhase(Level level, LivingEntity caster, Vec3 destination, float yaw,
                            boolean arriving, long deadline) {
        TheLastSwordMod.queueServerWork(1, () -> {
            if (!caster.isAlive() || caster.isRemoved() || caster.level() != level
                || (caster instanceof ServerPlayer player
                    && player.getServer().getPlayerList().getPlayer(player.getUUID()) != player)) {
                finishBlink(caster, false);
                return;
            }
            // 使用世界时间保证裂缝完整展开，不受队列在刻末执行的偏移影响。
            if (level.getGameTime() < deadline) {
                queuePhase(level, caster, destination, yaw, arriving, deadline);
                return;
            }
            if (!safeLanding(level, caster, destination)) {
                if (caster instanceof ServerPlayer player) {
                    player.displayClientMessage(Component.translatable("ui.the_last_sword.blink.no_destination"), true);
                }
                finishBlink(caster, false);
                return;
            }
            if (!arriving) {
                sendRift(caster, destination, yaw, 1);
                level.playSound(null, caster.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    caster.getSoundSource(), 1.0F, 1.0F);
                queuePhase(level, caster, destination, yaw, true, level.getGameTime() + RIFT_OPEN_TICKS);
                return;
            }
            boolean teleported = false;
            try {
                Vec3 origin = caster.position();
                Vec3 actual = EntityUtil.theLastEndSafeTeleport(caster, destination, 0);
                if (actual == null) return;
                teleported = true;
                caster.resetFallDistance();
                level.playSound(null, actual.x, actual.y, actual.z,
                    SoundEvents.ENDERMAN_TELEPORT, caster.getSoundSource(), 1.0F, 1.0F);
                ParticleUtil.spawnTeleportParticles(level, origin, actual, caster.getBbHeight());
                QueenBlinkSettings settings = TheLastSwordConfiguration.getQueenBlinkSettings();
                QueenBlinkBlade blade = new QueenBlinkBlade(level, caster,
                    new QueenBlinkSettings(settings.cooldown(), 20, settings.speed(),
                        settings.width(), settings.height(), settings.length()));
                if (level.addFreshEntity(blade)) {
                    level.playSound(null, caster.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT,
                        caster.getSoundSource(), 1.0F, 1.0F);
                }
            } finally {
                finishBlink(caster, teleported);
            }
        });
    }

    private void finishBlink(LivingEntity caster, boolean success) {
        activeBlinks.remove(caster.getUUID());
        sendRift(caster, caster.position(), caster.getYRot(), success ? 3 : 2);
    }

    private static boolean safeLanding(Level level, LivingEntity caster, Vec3 destination) {
        AABB standingBox = caster.getDimensions(Pose.STANDING).makeBoundingBox(destination);
        return level.hasChunkAt(BlockPos.containing(destination))
            && standingBox.minY >= level.getMinBuildHeight() && standingBox.maxY <= level.getMaxBuildHeight()
            && level.getWorldBorder().isWithinBounds(standingBox)
            && level.noCollision(caster, standingBox) && !level.containsAnyLiquid(standingBox)
            && EntityUtil.findSafeTeleportPosition(caster, destination, 0) != null;
    }

    private static Destination inspectDestination(Level level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        // 延长探测只用于区分超距与没有目标，实际传送仍严格限制在等级距离内。
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(caster.getLookAngle().scale(Math.max(range, 128))),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return new Destination(null, false);
        }
        if (eye.distanceToSqr(hit.getLocation()) > range * range) {
            return new Destination(null, true);
        }
        BlockPos pos = hit.getBlockPos();
        if (!level.hasChunkAt(pos)) {
            return new Destination(null, false);
        }
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos, CollisionContext.of(caster));
        if (shape.isEmpty()) {
            return new Destination(null, false);
        }
        Vec3 destination = new Vec3(pos.getX() + 0.5, pos.getY() + shape.max(Direction.Axis.Y), pos.getZ() + 0.5);
        if (caster.position().distanceToSqr(destination) > range * range) {
            return new Destination(null, true);
        }
        // 检查正常站立尺寸，避免蹲姿或游泳姿态允许传入无法站立的夹缝。
        AABB standingBox = caster.getDimensions(Pose.STANDING).makeBoundingBox(destination);
        if (standingBox.minY < level.getMinBuildHeight() || standingBox.maxY > level.getMaxBuildHeight()
            || !level.getWorldBorder().isWithinBounds(standingBox)
            || !level.noCollision(caster, standingBox) || level.containsAnyLiquid(standingBox)) {
            return new Destination(null, false);
        }
        return new Destination(EntityUtil.findSafeTeleportPosition(caster, destination, 0), false);
    }

    private record Destination(Vec3 position, boolean tooFar) {}
}
