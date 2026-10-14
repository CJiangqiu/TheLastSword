package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.compat.projecte.TransmutationStaffTermination;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.the_last_sword.compat.projecte.TransmutationStaffActions;
import net.minecraft.world.item.UseAnim;
import net.the_last_sword.compat.projecte.TransmutationStaffBehavior;
import net.the_last_sword.compat.projecte.client.TransmutationStaffClientExtension;
import net.the_last_sword.compat.projecte.StaffOfUniversalTransmutationItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

// 仅在可选施法系统存在时实例化，继承完整的施法、中断及提示行为。
public class CastingTransmutationStaffItem extends StaffItem implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CastingTransmutationStaffItem() {
        super(StaffOfUniversalTransmutationItem.createProperties(),
            new StaffTier((float) StaffOfUniversalTransmutationItem.ATTACK_DAMAGE_BONUS,
                (float) StaffOfUniversalTransmutationItem.ATTACK_SPEED_MODIFIER));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        StaffArmPose.initializeClientHelper(pose -> consumer.accept(new TransmutationStaffClientExtension(pose)));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return TransmutationStaffActions.createChargeProvider();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide) {
            TransmutationStaffBehavior.initialize(stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (TransmutationStaffBehavior.isStaffMode(player.getItemInHand(hand))) {
            return super.use(level, player, hand);
        }
        return TransmutationStaffBehavior.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return TransmutationStaffBehavior.useOn(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return TransmutationStaffBehavior.useOn(context);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return TransmutationStaffBehavior.getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * TransmutationStaffBehavior.getCharge(stack) / TransmutationStaffBehavior.MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAA55FF;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TransmutationStaffBehavior.appendTooltip(stack, tooltip, true);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (TransmutationStaffBehavior.isTermination(stack)) {
            return TransmutationStaffBehavior.getTerminationUseTicks();
        }
        return TransmutationStaffBehavior.isTransmutation(stack) ? 0 : super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (TransmutationStaffBehavior.isTermination(stack)) {
            return UseAnim.BOW;
        }
        return TransmutationStaffBehavior.isTransmutation(stack) ? UseAnim.NONE : super.getUseAnimation(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (TransmutationStaffBehavior.isTermination(stack)) {
            TransmutationStaffTermination.finish(stack, user);
            return stack;
        }
        return super.finishUsingItem(stack, level, user);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (TransmutationStaffBehavior.isStaffMode(stack)) {
            super.releaseUsing(stack, level, user, remaining);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (TransmutationStaffBehavior.isStaffMode(stack)) {
            super.onUseTick(level, user, stack, remaining);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
