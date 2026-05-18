package net.the_last_sword.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.entity.DragonSwordProjectile;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import net.the_last_sword.util.nbt.ItemModeHelper;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * 龙之剑 —— 普通模式（发射弹丸）/ 唤灵模式（召唤或收回剑灵）
 */
public class DragonSword extends TheLastEndSwordItems implements ISummonableItem {

    private static final int MAX_MODES = 2; // 0=普通模式, 1=唤灵模式

    public DragonSword() {
        super(
            new Tier() {
                @Override
                public int getUses() {
                    return 4096;
                }

                @Override
                public float getSpeed() {
                    return 9f;
                }

                @Override
                public float getAttackDamageBonus() {
                    return 196f;
                }

                @Override
                public int getLevel() {
                    return 5;
                }

                @Override
                public int getEnchantmentValue() {
                    return 50;
                }

                @Override
                public Ingredient getRepairIngredient() {
                    return Ingredient.of(new ItemStack(Items.DRAGON_EGG));
                }
            },
            3,
            -0.8f,
            new Item.Properties().fireResistant().rarity(Rarity.RARE),
            128f,
            200
        );
    }

    //龙之剑初始等级6级
    @Override
    protected int getDefaultLevel() {
        return 6;
    }

    //背包刻更新：初始化模式NBT
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);
    }

    //近战攻击：造成物理伤害+龙息伤害
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //调用基类方法，检查友方误伤并造成物理伤害
        if (!super.hurtEnemy(stack, target, attacker)) return false;

        //造成额外龙息伤害
        if (!attacker.level().isClientSide) {
            int level = ItemLevelHelper.getLevel(stack);
            double configValue = (level < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (level * configValue);

            if (extraDamage > 0) {
                //所有模式统一使用龙息伤害类型
                DamageSource damageSource = new DamageSource(
                    attacker.getCommandSenderWorld().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.DRAGON_BREATH),
                    attacker,
                    attacker
                );

                target.invulnerableTime = 0;
                target.hurt(damageSource, extraDamage);
            }
        }

        return true;
    }

    //右键使用
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //如果副手持盾且配置启用了盾牌取消使用，则传递事件给盾牌
        if (player.getOffhandItem().getItem() == Items.SHIELD &&
            TheLastSwordConfiguration.getBlockCancelUseSafely()) {
            return InteractionResultHolder.pass(stack);
        }

        int mode = ItemModeHelper.getMode(stack);

        if (mode == 0) {
            //普通模式：发射弹射物
            if (!world.isClientSide) {
                DragonSwordProjectile.shoot(world, player, player.getRandom(), player.getUUID(), this.getBasePhysicalDamage());
            }
        } else {
            //唤灵模式：召唤或唤回剑灵
            if (!world.isClientSide) {
                WraithSummonManager.summonWraith(player, stack, world);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, level, list, flag);

        //显示当前模式
        int mode = ItemModeHelper.getMode(stack);
        String modeKey = (mode == 0) ? "normal_mode" : "summon_entity_mode";
        list.add(Component.translatable("item_tooltip.the_last_sword.mode")
                .append(" ")
                .append(Component.translatable("item_tooltip.the_last_sword." + modeKey)));

        //未按Shift时，提示玩家按下Shift查看详情
        if (!Screen.hasShiftDown()) {
            list.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            //按住Shift时，显示操作描述
            int itemLevel = getItemLevel(stack);

            double configValue = (itemLevel < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (itemLevel * configValue);
            float basePhysicalDamage = this.getBasePhysicalDamage();

            if (mode == 0) {
                //常规模式操作说明
                list.add(Component.translatable("item_tooltip.the_last_sword.dragon_sword.left_click_normal",
                    String.format("%.0f", basePhysicalDamage),
                    String.format("%.0f", extraDamage)));
                list.add(Component.translatable("item_tooltip.the_last_sword.dragon_sword.right_click_normal",
                    String.format("%.0f", basePhysicalDamage),
                    String.format("%.0f", extraDamage)));
            } else {
                //唤灵模式操作说明
                list.add(Component.translatable("item_tooltip.the_last_sword.dragon_sword.summon_mode"));
            }
        }

        //Lore提示
        list.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_sword")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    protected boolean canMineInCurrentMode(ItemStack stack) {
        int mode = ItemModeHelper.getMode(stack);
        if (mode == 0) return TheLastSwordConfiguration.getDragonSwordNormalModeCanMineSafely();
        return TheLastSwordConfiguration.getDragonSwordSummonModeCanMineSafely();
    }

    //实现 ISummonableItem 接口
    @Override
    public int getSummonCooldownTicks() {
        return TheLastSwordConfiguration.getDragonSwordSummonCooldownSafely();
    }
}
