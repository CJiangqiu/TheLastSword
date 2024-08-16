package net.thelastsword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.capability.SwordLevelProvider;
import net.thelastsword.capability.ExtraAttackDamageProvider;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.entity.TheLastSwordProjectile;
import net.thelastsword.init.TheLastSwordModItems;

import java.util.List;

public class TheLastSword extends SwordItem implements ICapabilityProvider {
    private final float baseAttackDamage;

    public TheLastSword() {
        super(new Tier() {
            public int getUses() {
                return 0;
            }

            public float getSpeed() {
                return 1024f;
            }

            public float getAttackDamageBonus() {
                return 1022f;
            }

            public int getLevel() {
                return 1024;
            }

            public int getEnchantmentValue() {
                return 1024;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL.get()));
            }
        }, 3, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
        this.baseAttackDamage = 1022f;
    }

    public float getBaseAttackDamage() {
        return baseAttackDamage;
    }
    @Override
    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if (entity instanceof Player player) {
            boolean hasSword = false;
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof TheLastSword) {
                    hasSword = true;
                    break;
                }
            }

            if (hasSword && !player.isCreative() && !player.isSpectator()) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
                if (!player.getAbilities().invulnerable) {
                    player.getAbilities().invulnerable = true;
                }
            } else {
                if (player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                if (player.getAbilities().invulnerable) {
                    player.getAbilities().invulnerable = false;
                }
            }
        }
    }
    @Override
    public boolean isCorrectToolForDrops(BlockState blockstate) {
        int tier = 1024;
        if (tier < 3 && blockstate.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (tier < 2 && blockstate.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return tier < 1 && blockstate.is(BlockTags.NEEDS_STONE_TOOL)
                    ? false
                    : (blockstate.is(BlockTags.MINEABLE_WITH_AXE) || blockstate.is(BlockTags.MINEABLE_WITH_HOE) || blockstate.is(BlockTags.MINEABLE_WITH_PICKAXE) || blockstate.is(BlockTags.MINEABLE_WITH_SHOVEL));
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) || ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction)
                || ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
        return 1024f;
    }

    @Override
    public boolean mineBlock(ItemStack itemstack, Level world, BlockState blockstate, BlockPos pos, LivingEntity entity) {
        itemstack.hurtAndBreak(1, entity, i -> i.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack itemstack) {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        if (cap == SwordCapability.SWORD_LEVEL_CAPABILITY) {
            return LazyOptional.of(() -> (T) new SwordLevelProvider(13)).cast();
        } else if (cap == SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY) {
            return LazyOptional.of(() -> (T) new ExtraAttackDamageProvider(13)).cast();
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

            float totalDamage = (float) (extraDamage + this.getBaseAttackDamage());
            target.hurt(new DamageSource(attacker.getCommandSenderWorld().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)), totalDamage);
        });
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        itemstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.dragon_sword").append(" " + swordLevel.getLevel()));
        });

        itemstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.extra_attack_damage")
                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA00FF)))
                    .append(" " + extraAttackDamage.getExtraAttackDamage()));
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            TheLastSwordProjectile.shoot(world, player, RandomSource.create());
            itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
        }
        return InteractionResultHolder.success(itemstack);
    }
}
