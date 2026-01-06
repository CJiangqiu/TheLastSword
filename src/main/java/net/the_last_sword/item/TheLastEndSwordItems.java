package net.the_last_sword.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.List;

/**
 * 最终之剑系列物品的抽象基类
 * 包括：龙水晶剑、龙之剑、最终之剑
 */
public abstract class TheLastEndSwordItems extends SwordItem {
    private final float destroySpeed;
    private final int toolLevel;

    public TheLastEndSwordItems(Tier tier, int attackDamageModifier, float attackSpeedModifier,
                                Properties properties, float destroySpeed, int toolLevel) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.destroySpeed = destroySpeed;
        this.toolLevel = toolLevel;
    }

    //检查是否是正确的工具来挖掘方块
    @Override
    public boolean isCorrectToolForDrops(BlockState blockstate) {
        int tier = toolLevel;
        if (tier < 3 && blockstate.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (tier < 2 && blockstate.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return tier >= 1 || !blockstate.is(BlockTags.NEEDS_STONE_TOOL);
        }
    }

    //获取破坏方块的速度
    @Override
    public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
        return destroySpeed;
    }

    //支持所有工具的默认动作（可以当作剑、镐、斧、锄、铲使用）
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction) ||
               ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction) ||
               ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(toolAction) ||
               ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) ||
               ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    //计算基础物理伤害：Tier的攻击伤害 + 4（公共方法，供tooltip和弹射物使用）
    public float getBasePhysicalDamage() {
        return this.getTier().getAttackDamageBonus() + 4f;
    }

    //获取默认等级（子类可覆盖）
    protected int getDefaultLevel() {
        return 0; // 龙水晶剑默认0级
    }

    //获取物品等级（自动初始化）
    protected int getItemLevel(ItemStack itemstack) {
        ItemLevelHelper.ensureInitialized(itemstack, getDefaultLevel());
        return ItemLevelHelper.getLevel(itemstack);
    }

    //重写伤害逻辑：检查友方误伤，清除无敌时间允许子类添加额外伤害
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //友方检查：如果是友方则跳过所有额外伤害逻辑
        if (!EntityUtil.canAttack(attacker, target)) return false;

        boolean result = super.hurtEnemy(stack, target, attacker);

        //清除目标的无敌时间，让后续的额外伤害能够生效（避免伤害冷却覆盖问题）
        target.invulnerableTime = 0;

        return result;
    }

    //添加工具提示信息（显示等级和额外攻击伤害）
    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        // 获取等级
        int itemLevel = getItemLevel(itemstack);
        list.add(Component.translatable("item_tooltip.the_last_sword.level").append(" " + itemLevel));

        // 实时计算额外攻击伤害（确保配置值更改时能及时更新显示）
        double configValue = (itemLevel < 6)
                ? TheLastSwordConfiguration.getIncreaseValueSafely()
                : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
        float realTimeExtraDamage = (float) (itemLevel * configValue);

        list.add(Component.translatable("item_tooltip.the_last_sword.extra_attack_damage")
                .withStyle(style -> style.withColor(TextColor.fromRgb(0xAA00FF)))
                .append(" " + String.format("%.0f", realTimeExtraDamage)));
    }

    //可以在附魔台接受的附魔（接受剑、镐、弓的所有附魔）
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.canEnchant(new ItemStack(Items.NETHERITE_SWORD).getItem().getDefaultInstance()) ||
               enchantment.canEnchant(new ItemStack(Items.NETHERITE_PICKAXE).getItem().getDefaultInstance()) ||
               enchantment.canEnchant(new ItemStack(Items.BOW).getItem().getDefaultInstance()) ||
               super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canBeHurtBy(DamageSource damageSource) {
        return false;
    }
}
