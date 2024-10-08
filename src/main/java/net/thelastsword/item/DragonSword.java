package net.thelastsword.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.thelastsword.capability.DefaultExtraAttackDamage;
import net.thelastsword.capability.DefaultSwordLevel;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.entity.DragonCrystalSwordProjectile;
import net.thelastsword.entity.DragonSwordProjectile;

import java.util.List;

public class DragonSword extends SwordItem implements ICapabilityProvider {

    // Constructor/构造函数
    public DragonSword() {
        super(new Tier() {
            public int getUses() {
                return 4096;
            }

            public float getSpeed() {
                return 4.5f;
            }

            public float getAttackDamageBonus() {
                return 96f;
            }

            public int getLevel() {
                return 5;
            }

            public int getEnchantmentValue() {
                return 50;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(Items.DRAGON_EGG));
            }
        }, 3, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.RARE));

    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockstate) {
        int level = 5;
        if (level < 3 && blockstate.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (level < 2 && blockstate.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return level < 1 && blockstate.is(BlockTags.NEEDS_STONE_TOOL)
                    ? false
                    : (blockstate.is(BlockTags.MINEABLE_WITH_AXE) || blockstate.is(BlockTags.MINEABLE_WITH_HOE) || blockstate.is(BlockTags.MINEABLE_WITH_PICKAXE) || blockstate.is(BlockTags.MINEABLE_WITH_SHOVEL));
        }
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
        return 128f;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)
                || ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            DragonSwordProjectile.shoot(world, player, player.getRandom());
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        if (cap == SwordCapability.SWORD_LEVEL_CAPABILITY) {
            return LazyOptional.of(() -> (T) new DefaultSwordLevel(0)).cast();
        } else if (cap == SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY) {
            return LazyOptional.of(() -> (T) new DefaultExtraAttackDamage(0)).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            int level = swordLevel.getLevel();
            double increaseValue = TheLastSwordConfiguration.INCREASE_VALUE.get();
            double increaseValueHighLevel = TheLastSwordConfiguration.INCREASE_VALUE_HIGH_LEVEL.get();
            double extraDamage = (level < 6 ? increaseValue : increaseValueHighLevel) * level;

            stack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
                extraAttackDamage.setExtraAttackDamage((float) extraDamage);
            });

            float totalDamage = (float) (extraDamage + getTier().getAttackDamageBonus());
            target.hurt(new DamageSource(attacker.getCommandSenderWorld().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), totalDamage);
        });
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        itemstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.sword_level").append(" " + swordLevel.getLevel()));
        });

        itemstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.extra_attack_damage")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA00FF)))
                    .append(" " + extraAttackDamage.getExtraAttackDamage()));
        });
    }
}
