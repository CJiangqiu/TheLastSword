package net.the_last_sword.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.TheLastEndSwordProjectile;
import net.the_last_sword.init.ModKeyMappings;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import net.the_last_sword.util.nbt.ItemModeHelper;
import net.the_last_sword.event.ServerEventHandler;
import net.minecraftforge.common.ForgeMod;

import java.util.List;

//最终之剑 - 3种模式: 0=常规(弹射物+范围攻击), 1=挖掘, 2=召唤
@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
public class TheLastSword extends TheLastEndSwordItems {

    private static final int MAX_MODES = 3; // 0=常规模式, 1=挖掘模式, 2=召唤模式

    //构造函数：设置自定义Tier及物品属性
    public TheLastSword() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0; //无限耐久
            }

            @Override
            public float getSpeed() {
                return 13f;
            }

            @Override
            public float getAttackDamageBonus() {
                return 196f; //高额攻击伤害
            }

            @Override
            public int getLevel() {
                return 1024; //极高的挖掘等级
            }

            @Override
            public int getEnchantmentValue() {
                return 1024; //高附魔等级
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(Items.DRAGON_EGG)); //修复材料
            }
        }, 3, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.EPIC), 1024f, 200);
    }

    @Override
    protected int getDefaultLevel() {
        return 13; //最终之剑默认13级
    }

    //背包刻更新
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        //初始化模式系统（如果还没有）
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);
    }

    //近战单体攻击：模式0造成绝毁伤害，模式1和2造成虚空伤害
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //调用基类方法，检查友方误伤并造成物理伤害
        if (!super.hurtEnemy(stack, target, attacker)) return false;

        if (!attacker.level().isClientSide) {
            int mode = ItemModeHelper.getMode(stack);
            int level = ItemLevelHelper.getLevel(stack);
            double configValue = (level < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            //计算额外伤害：基础伤害 + 敌人13%最大生命值
            float baseDamage = (float) (level * configValue);
            float percentageDamage = target.getMaxHealth() * 0.13f;
            float extraDamage = baseDamage + percentageDamage;
            if (extraDamage > 0) {
                target.invulnerableTime = 0;
                if (mode == 0) {
                    //模式0：物理伤害 + 绝毁伤害
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, attacker, stack, extraDamage);
                } else if (mode == 1 || mode == 2) {
                    //模式1和2：物理伤害 + 虚空伤害
                    DamageSource voidDamageSource = new DamageSource(
                            attacker.getCommandSenderWorld().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD), attacker, attacker);
                    target.hurt(voidDamageSource, extraDamage);
                }
            }
        }
        return true;
    }

    private boolean returnSwing;

    //物品使用（右键）处理：
    //模式0：发射弹射物
    //模式1：执行强力范围挖掘
    //模式2：召唤或收回剑灵
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.getOffhandItem().getItem() == Items.SHIELD && TheLastSwordConfiguration.getBlockCancelUseSafely()) {
            return InteractionResultHolder.pass(itemstack); //副手使用盾牌时让事件传递给盾牌
        }

        int mode = ItemModeHelper.getMode(itemstack);

        if (mode == 0) {
            //模式0：右键发射弹射物
            if (!world.isClientSide) {
                TheLastEndSwordProjectile.shoot(world, player, RandomSource.create());
            }
        } else if (mode == 1) {
            //模式1：右键进行强力范围挖掘（带预览系统）
            if (!world.isClientSide) {
                ServerEventHandler.performMining(player, world);
            }
        } else if (mode == 2) {
            //模式2：右键唤灵（召唤或唤回剑灵）
            if (!world.isClientSide) {
                returnSwing = true;
                WraithSummonManager.summonWraith(player, itemstack, world);
            }
        }
        return InteractionResultHolder.success(itemstack);
    }

    //左键挥动触发范围攻击：仅模式0生效
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (returnSwing) {
            returnSwing = false;
            return true;
        }
        if (entity instanceof Player player && !entity.level().isClientSide) {
            int mode = ItemModeHelper.getMode(stack);
            if (mode == 0) {
                performNormalModeForwardAreaAttack(player, stack);
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    //常规模式的正前方范围攻击
    private void performNormalModeForwardAreaAttack(Player player, ItemStack stack) {
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 playerPos = player.position();

        double attackRange = player.getAttributeValue(ForgeMod.ENTITY_REACH.get());

        Vec3 endPos = playerPos.add(lookVec.scale(attackRange));
        AABB attackBox = new AABB(
            Math.min(playerPos.x, endPos.x) - attackRange/2,
            playerPos.y - 1,
            Math.min(playerPos.z, endPos.z) - attackRange/2,
            Math.max(playerPos.x, endPos.x) + attackRange/2,
            playerPos.y + 2,
            Math.max(playerPos.z, endPos.z) + attackRange/2
        );

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, attackBox,
            e -> e != player && e.isAlive() &&
            EntityUtil.canAttack(player, e) &&
            !WraithSummonManager.isWraith(e.getUUID()));

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().subtract(playerPos).normalize();
            double dot = lookVec.dot(toTarget);
            if (dot > 0.5) {
                int level = ItemLevelHelper.getLevel(stack);
                double configValue = (level < 6)
                        ? TheLastSwordConfiguration.getIncreaseValueSafely()
                        : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
                //计算额外伤害：基础伤害 + 敌人13%最大生命值
                float baseDamage = (float) (level * configValue);
                float percentageDamage = target.getMaxHealth() * 0.13f;
                float extraDamage = baseDamage + percentageDamage;

                //先造成物理伤害
                float basePhysicalDamage = getBasePhysicalDamage();
                target.hurt(player.damageSources().playerAttack(player), basePhysicalDamage);

                //再造成绝毁伤害
                if (extraDamage > 0) {
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(target, player, stack, extraDamage);
                }
            }
        }
    }

    //增加工具提示：显示当前模式及其说明（支持Shift展开详细描述）
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        //4. 模式行：Mode: <当前模式名称>
        int mode = ItemModeHelper.getMode(stack);
        tooltip.add(
                Component.translatable("item_tooltip.the_last_sword.mode")
                        .append(" ")
                        .append(Component.translatable(getModeTextKey(mode)))
        );

        //5. 未按Shift时，提示玩家按下Shift查看详情
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            //5. 按住Shift时，首先显示操作描述，然后显示模式描述
            int itemLevel = getItemLevel(stack);
            double configValue = (itemLevel < 6)
                    ? TheLastSwordConfiguration.getIncreaseValueSafely()
                    : TheLastSwordConfiguration.getIncreaseValueHighLevelSafely();
            float extraDamage = (float) (itemLevel * configValue);
            float basePhysicalDamage = getBasePhysicalDamage();

            //根据模式添加操作描述（附加伤害包含13%敌人生命）
            switch (mode) {
                case 0 -> {
                    //常规模式操作说明
                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.left_click_normal",
                        String.format("%.0f", basePhysicalDamage), String.format("%.0f", extraDamage)));
                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.right_click_normal",
                        String.format("%.0f", basePhysicalDamage), String.format("%.0f", extraDamage)));
                }
                case 1 -> {
                    //强力挖掘模式操作说明
                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.left_click_mining",
                        String.format("%.0f", basePhysicalDamage), String.format("%.0f", extraDamage)));
                    int miningRadius = TheLastSwordConfiguration.getMiningRadiusSafely();
                    int miningArea = (miningRadius * 2 + 1);
                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.right_click_mining",
                        miningArea + "x" + miningArea + "x" + miningArea));
                }
                case 2 -> {
                    //唤灵模式操作说明
                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.left_click_summon",
                        String.format("%.0f", basePhysicalDamage), String.format("%.0f", extraDamage)));

                    //根据等级选择对应的每级加成（6级为分界点）
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

                    tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.right_click_summon",
                        String.format("%.0f", healthBonus), String.format("%.0f", attackBonus)));
                }
            }

            //添加空行分隔操作说明和详细描述
            tooltip.add(Component.empty());

            //然后显示详细的模式描述
            switch (mode) {
                case 1 -> {
                    int miningRadius = TheLastSwordConfiguration.getMiningRadiusSafely();
                    tooltip.add(
                            Component.translatable("item_tooltip.the_last_sword.powerful_mining_mode_descr", miningRadius)
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)))
                    );
                }
                case 2 -> {
                    //动态计算剑灵加成数值并传递给详细描述
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

                    tooltip.add(
                            Component.translatable("item_tooltip.the_last_sword.summon_entity_mode_descr",
                                    String.format("%.0f", healthBonus),
                                    String.format("%.0f", attackBonus))
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA))));
                }
                default -> {
                    tooltip.add(
                            Component.translatable("item_tooltip.the_last_sword.normal_mode_descr")
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)))
                    );
                }
            }
        }

        //6. 被动技能：终焉之主
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_last_sword.passive"));

        //7. 当前模式切换绑定按键
        tooltip.add(
                Component.translatable("item_tooltip.the_last_sword.mode_key")
                        .append(" ")
                        .append(ModKeyMappings.CHANGE_SWORD_MODE.getKey().getDisplayName().getString())
        );

        //8. Lore提示
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_last_sword")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    //获取模式翻译键
    private static String getModeTextKey(int mode) {
        return switch (mode) {
            case 0 -> "item_tooltip.the_last_sword.normal_mode";
            case 1 -> "item_tooltip.the_last_sword.powerful_mining_mode";
            case 2 -> "item_tooltip.the_last_sword.summon_entity_mode";
            default -> "item_tooltip.the_last_sword.normal_mode";
        };
    }

    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
    public static class TheLastSwordTickHandler {

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            if (event.player.level().isClientSide) return;

            Player player = event.player;
            boolean hasSword = hasTheLastSwordInInventory(player);

            //飞行逻辑
            if (!player.isCreative() && !player.isSpectator()) {
                boolean allowFlying = TheLastSwordConfiguration.getAllowFlyingSafely();
                if (hasSword) {
                    if (allowFlying && !player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = true;
                        player.getPersistentData().putBoolean("TheLastSwordFly", true);
                        player.onUpdateAbilities();
                    }
                } else {
                    if (player.getPersistentData().getBoolean("TheLastSwordFly")) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.getPersistentData().remove("TheLastSwordFly");
                        player.onUpdateAbilities();
                    }
                }
            }

            //防御逻辑
            if (hasSword) {
                EntityUtil.registerDefence(player, player.getMaxHealth());
                player.getPersistentData().putBoolean("TheLastSwordDefence", true);
            } else {
                if (player.getPersistentData().getBoolean("TheLastSwordDefence")) {
                    EntityUtil.clearDefence(player);
                    player.getPersistentData().remove("TheLastSwordDefence");
                }
            }

            //无冷却逻辑：持有最终之剑时，清除所有物品的冷却时间
            if (hasSword) {
                //清除主物品栏所有物品的冷却
                for (ItemStack stack : player.getInventory().items) {
                    if (!stack.isEmpty()) {
                        player.getCooldowns().removeCooldown(stack.getItem());
                    }
                }
                //清除副手物品的冷却
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    player.getCooldowns().removeCooldown(offhand.getItem());
                }
            }
        }

        //检查玩家背包中是否有最终之剑
        private static boolean hasTheLastSwordInInventory(Player player) {
            if (player == null) return false;
            for (ItemStack s : player.getInventory().items) {
                if (s.getItem() instanceof TheLastSword) {
                    return true;
                }
            }
            ItemStack offhand = player.getOffhandItem();
            return offhand.getItem() instanceof TheLastSword;
        }
    }
}
