package net.the_last_sword.compat.projecte;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.the_last_sword.compat.projecte.client.TransmutationStaffClientExtension;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class StaffOfUniversalTransmutationItem extends Item implements GeoItem {
    public static final double ATTACK_DAMAGE_BONUS = 9.0;
    public static final double ATTACK_SPEED_MODIFIER = -2.4;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public StaffOfUniversalTransmutationItem() {
        super(createProperties());
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers = ImmutableMultimap.builder();
        modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
            BASE_ATTACK_DAMAGE_UUID, "Universal transmutation staff attack damage",
            ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADDITION));
        // 玩家基础攻速为 4，减去 2.4 后达到每秒 1.6 次攻击。
        modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(
            BASE_ATTACK_SPEED_UUID, "Universal transmutation staff attack speed",
            ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADDITION));
        defaultModifiers = modifiers.build();
    }

    public static Properties createProperties() {
        return new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new TransmutationStaffClientExtension(IClientItemExtensions.DEFAULT));
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
        return TransmutationStaffBehavior.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return TransmutationStaffBehavior.isTermination(stack) ? TransmutationStaffBehavior.getTerminationUseTicks() : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return TransmutationStaffBehavior.isTermination(stack) ? UseAnim.BOW : UseAnim.NONE;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (TransmutationStaffBehavior.isTermination(stack)) {
            TransmutationStaffTermination.finish(stack, user);
        }
        return stack;
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
        TransmutationStaffBehavior.appendTooltip(stack, tooltip, false);
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
