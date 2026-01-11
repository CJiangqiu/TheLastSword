package net.the_last_sword.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

//龙魂灯笼物品 - 可召唤剑灵、放置为方块或作为腰带饰品
public class DragonSoulLanternItem extends BlockItem implements ICurioItem {

    public DragonSoulLanternItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        //初始化默认等级为1
        ItemLevelHelper.ensureInitialized(stack, 1);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //确保等级已初始化
        ItemLevelHelper.ensureInitialized(stack, 1);

        //非潜行状态下右键空气时召唤剑灵
        if (!player.isCrouching() && !world.isClientSide) {
            WraithSummonManager.summonWraith(player, stack, world);
            return InteractionResultHolder.success(stack);
        }

        //其他情况（潜行或客户端）使用默认行为
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        //潜行时或右键方块时，使用默认的方块放置逻辑
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            return super.useOn(context);
        }

        //非潜行状态右键方块时也召唤剑灵（不放置方块）
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide) {
            WraithSummonManager.summonWraith(player, stack, context.getLevel());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        //显示等级
        int itemLevel = ItemLevelHelper.getLevel(stack);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.level")
                .append(" " + itemLevel));

        //主标语
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_soul_lantern")
            .withStyle(net.minecraft.ChatFormatting.GRAY));

        //未按Shift时提示
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            //按住Shift显示详细描述
            //计算剑灵属性
            float healthPerLevel, attackPerLevel;
            if (itemLevel >= 6) {
                healthPerLevel = (float) TheLastSwordConfiguration.getSwordWraithHealthPerHighLevelSafely();
                attackPerLevel = (float) TheLastSwordConfiguration.getSwordWraithAttackPerHighLevelSafely();
            } else {
                healthPerLevel = (float) TheLastSwordConfiguration.getSwordWraithHealthPerLevelSafely();
                attackPerLevel = (float) TheLastSwordConfiguration.getSwordWraithAttackPerLevelSafely();
            }
            float healthBonus = itemLevel * healthPerLevel;
            float attackBonus = itemLevel * attackPerLevel;

            tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_soul_lantern_descr",
                    String.format("%.0f", healthBonus),
                    String.format("%.0f", attackBonus)));
        }
    }
}
