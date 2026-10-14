package net.the_last_sword.compat.projecte;

import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.EntityRandomizerHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldTransmutations;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.the_last_sword.entity.TheLastEndSwordProjectile;
import net.the_last_sword.init.ModEntities;

public final class TransmutationStaffActions {
    private TransmutationStaffActions() {
    }

    public static ICapabilityProvider createChargeProvider() {
        return new ChargeProvider();
    }

    public static InteractionResultHolder<ItemStack> openTable(Level level, Player player, InteractionHand hand) {
        return PEItems.TRANSMUTATION_TABLET.get().use(level, player, hand);
    }

    public static InteractionResultHolder<ItemStack> fireProjectile(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (!consumeCastingFuel(player, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            TheLastEndSwordProjectile projectile = new TheLastEndSwordProjectile(
                ModEntities.THE_LAST_END_SWORD_PROJECTILE.get(), player, level, player.getUUID());
            // 独立快照避免另一只手中的武器等级覆盖法杖充能伤害。
            projectile.setSnapshotDamage(TransmutationStaffBehavior.getProjectileDamage(stack), 0);
            projectile.shoot(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z, 4.0F, 0);
            projectile.setSilent(true);
            level.addFreshEntity(projectile);
            level.playSound(null, player.blockPosition(), PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static InteractionResult transmuteBlock(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !player.mayBuild()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockState result = WorldTransmutations.getWorldTransmutation(level.getBlockState(context.getClickedPos()), false);
        if (result != null && PlayerHelper.checkedReplaceBlock((ServerPlayer) player, context.getClickedPos(), result)) {
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                context.getClickedPos().getX() + 0.5, context.getClickedPos().getY() + 1,
                context.getClickedPos().getZ() + 0.5, 2, 0, 0, 0, 0);
            level.playSound(null, player.blockPosition(), PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResult.CONSUME;
    }

    public static InteractionResult interactEntity(Player player, InteractionHand hand, Entity target) {
        Level level = player.level();
        if (player.isShiftKeyDown()) {
            return openTable(level, player, hand).getResult();
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(target instanceof Mob original) || !original.isAlive()) {
            return InteractionResult.CONSUME;
        }
        Mob replacement = EntityRandomizerHelper.getRandomEntity(level, original);
        if (replacement == null) {
            return InteractionResult.CONSUME;
        }
        long cost = TheLastSwordConfiguration.getTransmutationStaffEntityEmcCostSafely();
        if (cost > 0 && EMCHelper.consumePlayerFuel(player, cost) == -1) {
            reportInsufficientFuel(player, cost);
            replacement.discard();
            return InteractionResult.CONSUME;
        }
        replacement.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
        SpawnGroupData spawnData = replacement instanceof Rabbit rabbit && rabbit.getVariant() == Rabbit.Variant.EVIL
            ? new Rabbit.RabbitGroupData(Rabbit.Variant.EVIL) : null;
        ServerLevel serverLevel = (ServerLevel) level;
        ForgeEventFactory.onFinalizeSpawn(replacement, serverLevel,
            serverLevel.getCurrentDifficultyAt(replacement.blockPosition()), MobSpawnType.CONVERSION, spawnData, null);
        serverLevel.tryAddFreshEntityWithPassengers(replacement);
        // 出生被取消时保留原生物，避免只删除而没有成功替换。
        if (replacement.isAddedToWorld()) {
            replacement.spawnAnim();
            original.discard();
            level.playSound(null, player.blockPosition(), PESoundEvents.TRANSMUTE.get(), SoundSource.PLAYERS, 1, 1);
        }
        return InteractionResult.CONSUME;
    }

    public static boolean consumeCastingFuel(Player player, ItemStack stack) {
        long cost = TransmutationStaffBehavior.getProjectileCost(stack);
        // 保留燃料面额的余量，避免每次消耗整份燃料而浪费差额。
        if (cost == 0 || ItemPE.consumeFuel(player, stack, cost, true)) {
            return true;
        }
        reportInsufficientFuel(player, cost);
        return false;
    }

    private static void reportInsufficientFuel(Player player, long cost) {
        player.displayClientMessage(Component.translatable(
            "item_tooltip.the_last_sword.transmutation_staff.insufficient_emc", cost), true);
    }

    private static final class ChargeProvider implements ICapabilityProvider, IItemCharge {
        private final LazyOptional<IItemCharge> charge = LazyOptional.of(() -> this);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == PECapabilities.CHARGE_ITEM_CAPABILITY ? charge.cast() : LazyOptional.empty();
        }

        @Override
        public int getNumCharges(ItemStack stack) {
            return TransmutationStaffBehavior.MAX_CHARGE;
        }

        @Override
        public int getCharge(ItemStack stack) {
            return TransmutationStaffBehavior.getCharge(stack);
        }
    }
}
