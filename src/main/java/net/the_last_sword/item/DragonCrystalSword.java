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
import net.the_last_sword.entity.DragonCrystalSwordProjectile;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.List;


public class DragonCrystalSword extends TheLastEndSwordItems {

    public DragonCrystalSword() {
        super(
            new Tier() {
                @Override
                public int getUses() {
                    return 2048;
                }

                @Override
                public float getSpeed() {
                    return 4.5f;
                }

                @Override
                public float getAttackDamageBonus() {
                    return 8f;
                }

                @Override
                public int getLevel() {
                    return 4;
                }

                @Override
                public int getEnchantmentValue() {
                    return 22;
                }

                @Override
                public Ingredient getRepairIngredient() {
                    return Ingredient.of(new ItemStack(ModItems.DRAGON_CRYSTAL.get()));
                }
            },
            3,
            -2.4f,
            new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON),
            64f,
            4
        );
    }

    //近战攻击：造成物理伤害+魔法伤害
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //调用基类方法，检查友方误伤并造成物理伤害
        if (!super.hurtEnemy(stack, target, attacker)) return false;

        //造成额外的魔法伤害
        if (!attacker.level().isClientSide) {
            int level = ItemLevelHelper.getLevel(stack);
            double configValue = (level < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (level * configValue);

            if (extraDamage > 0) {
                target.invulnerableTime = 0;
                target.hurt(
                    new DamageSource(
                        attacker.getCommandSenderWorld().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.MAGIC),
                        attacker,
                        attacker
                    ),
                    extraDamage
                );
            }
        }

        return true;
    }

    //右键使用：发射弹射物
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        //如果副手持盾且配置启用了盾牌取消使用，则传递事件给盾牌
        if (player.getOffhandItem().getItem() == Items.SHIELD &&
            TheLastSwordConfiguration.getBlockCancelUseSafely()) {
            return InteractionResultHolder.pass(itemstack);
        }

        //发射弹射物（传递剑的基础物理伤害）
        if (!world.isClientSide) {
            float projectileDamage = this.getBasePhysicalDamage();
            DragonCrystalSwordProjectile.shoot(world, player, player.getRandom(), player.getUUID(), projectileDamage);
        }

        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, level, list, flag);

        //未按Shift时，提示玩家按下Shift查看详情
        if (!Screen.hasShiftDown()) {
            list.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            //按住Shift时，显示操作描述
            int itemLevel = getItemLevel(itemstack);

            double configValue = (itemLevel < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (itemLevel * configValue);

            float basePhysicalDamage = this.getBasePhysicalDamage();


            list.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_sword.left_click",
                String.format("%.0f", basePhysicalDamage),
                String.format("%.0f", extraDamage)));
            list.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_sword.right_click",
                String.format("%.0f", basePhysicalDamage),
                String.format("%.0f", extraDamage)));
        }

        list.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_sword")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
