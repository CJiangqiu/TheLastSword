package net.the_last_sword.compat.projecte;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import net.eca.api.EcaAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.network.NetworkHandler;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.irons_spellbooks.TransmutationStaffMana;
import net.the_last_sword.entity.TheLastEndLightingEntity;
import net.the_last_sword.init.ModEntities;

public final class TransmutationStaffTermination {
    private static final int COOLDOWN_TICKS = 13 * 20;

    private TransmutationStaffTermination() {
    }

    public static InteractionResultHolder<ItemStack> begin(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                return InteractionResultHolder.fail(stack);
            }
            IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);
            if (knowledge == null || knowledge.getEmc().compareTo(BigInteger.TEN) < 0) {
                reportEmptyBalance(player);
                return InteractionResultHolder.fail(stack);
            }
            // 使用状态由服务端同步，避免余额不足时留下客户端预测的蓄力。
            player.startUsingItem(hand);
        }
        return InteractionResultHolder.consume(stack);
    }

    public static void finish(ItemStack stack, LivingEntity user) {
        if (!(user instanceof ServerPlayer player) || !player.isAlive()
            || !TransmutationStaffBehavior.isTermination(stack) || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }
        IKnowledgeProvider knowledge = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).orElse(null);
        if (knowledge == null || knowledge.getEmc().compareTo(BigInteger.TEN) < 0) {
            reportEmptyBalance(player);
            return;
        }
        Vec3 center = player.position();
        double radius = TransmutationStaffBehavior.TERMINATION_RADIUS;
        List<Entity> targets = player.level().getEntities(player, new AABB(center, center).inflate(radius),
            target -> target.isAlive() && target.position().distanceToSqr(center) <= radius * radius
                && EntityUtil.canAttack(player, target));
        List<LockedTarget> locks = new ArrayList<>(targets.size());
        try {
            for (Entity target : targets) {
                locks.add(new LockedTarget(target, EcaAPI.getLockedLocation(target), target.level().dimension()));
                EcaAPI.lockLocation(target);
            }
            BigInteger balance = knowledge.getEmc();
            BigInteger cost = balance.divide(BigInteger.TEN);
            if (cost.signum() <= 0) {
                reportEmptyBalance(player);
                return;
            }
            // 余额保持任意精度；伤害接口只接受有限浮点数，避免溢出后被拒绝。
            double baseDamage = new BigDecimal(cost).movePointLeft(1).doubleValue();
            knowledge.setEmc(balance.subtract(cost));
            knowledge.syncEmc(player);
            float manaSpent = CompatCheck.isSpellcastingLoaded()
                ? TransmutationStaffMana.drain(player) : 0.0F;
            float damage = (float) Math.min(baseDamage + manaSpent, Float.MAX_VALUE);
            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
            NetworkHandler.sendToTrackingClientsAndSelf(new TransmutationStaffTerminationPacket(
                player.level().dimension().location(), center), player);
            player.level().playSound(null, player.blockPosition(), PESoundEvents.TRANSMUTE.get(),
                SoundSource.PLAYERS, 1.0F, 0.5F);
            DamageSource source = AbsoluteDestructionDamageSource.absoluteDestruction(player, stack);
            for (Entity target : targets) {
                if (!target.isAlive() || target.level() != player.level() || !EntityUtil.canAttack(player, target)) {
                    continue;
                }
                TerminationLightning lightning = new TerminationLightning(player.serverLevel());
                lightning.moveTo(target.position());
                player.serverLevel().addFreshEntity(lightning);
                if (target instanceof LivingEntity living) {
                    EcaAPI.hurt(living, source, damage);
                } else {
                    target.hurt(source, damage);
                }
            }
        } finally {
            // 只恢复本次临时锁位，保留目标在释放前已有的限制。
            for (LockedTarget lock : locks) {
                if (lock.previousPosition() == null) {
                    EcaAPI.unlockLocation(lock.entity());
                } else if (lock.dimension() == lock.entity().level().dimension()) {
                    EcaAPI.lockLocation(lock.entity(), lock.previousPosition());
                } else {
                    EcaAPI.lockLocation(lock.entity());
                }
            }
        }
    }

    private static void reportEmptyBalance(Player player) {
        player.displayClientMessage(Component.translatable(
            "item_tooltip.the_last_sword.transmutation_staff.empty_balance"), true);
    }

    private static final class TerminationLightning extends TheLastEndLightingEntity {
        private int age;

        private TerminationLightning(ServerLevel level) {
            super(ModEntities.THE_LAST_END_LIGHTING.get(), level);
            setVisualOnly(true);
        }

        @Override
        public boolean shouldBeSaved() {
            return false;
        }

        @Override
        public void tick() {
            // 客户端按注册类型呈现原有闪电；服务端跳过雷击、铜块与避雷针逻辑。
            if (++age >= 20) {
                discard();
            }
        }
    }

    private record LockedTarget(Entity entity, Vec3 previousPosition, ResourceKey<Level> dimension) {
    }
}
