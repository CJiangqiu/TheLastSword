package net.thelastsword.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.thelastsword.capability.DefaultExtraAttackDamage;
import net.thelastsword.capability.DefaultSwordLevel;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.entity.DragonCrystalSwordProjectile;
import net.thelastsword.init.TheLastSwordModItems;

import java.util.List;

public class DragonCrystalSword extends SwordItem implements ICapabilityProvider {
    private final float baseAttackDamage;

    public DragonCrystalSword() {
        super(new Tier() {
            public int getUses() {
                return 2048;
            }

            public float getSpeed() {
                return 4.5f;
            }

            public float getAttackDamageBonus() {
                return 8f;
            }

            public int getLevel() {
                return 1;
            }

            public int getEnchantmentValue() {
                return 22;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL.get()));
            }
        }, 3, -2.4f, new Item.Properties().rarity(Rarity.UNCOMMON).fireResistant());
        this.baseAttackDamage = 8f; // 设置基础攻击伤害
    }

    public float getBaseAttackDamage() {
        return baseAttackDamage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            DragonCrystalSwordProjectile.shoot(world, player, player.getRandom());
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
}
