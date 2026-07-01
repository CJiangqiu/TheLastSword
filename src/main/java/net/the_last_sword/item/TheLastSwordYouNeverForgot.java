package net.the_last_sword.item;

import net.eca.api.EcaAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.entity.TheLastSwordYouNeverForgotProjectile;
import net.the_last_sword.util.EntityUtil;

import java.util.List;

// 隐藏武器 - 无模式/等级系统，纯粹的高额伤害+弹射物
public class TheLastSwordYouNeverForgot extends TheLastEndSwordItems {

    public static final float ABSOLUTE_DESTRUCTION_DAMAGE = 7999999874453995500f;

    public TheLastSwordYouNeverForgot() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 1024f;
            }

            @Override
            public float getAttackDamageBonus() {
                return ABSOLUTE_DESTRUCTION_DAMAGE;
            }

            @Override
            public int getLevel() {
                return 1024;
            }

            @Override
            public int getEnchantmentValue() {
                return 1024;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }
        }, 0, -2.4f, new Item.Properties().fireResistant().rarity(Rarity.EPIC), 1024f, 1024);
    }

    @Override
    protected int getDefaultLevel() {
        return 0;
    }

    // 始终允许挖掘，无模式限制
    @Override
    protected boolean canMineInCurrentMode(ItemStack stack) {
        return true;
    }

    // 左键：基础物理伤害 + 自定义attack
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!super.hurtEnemy(stack, target, attacker)) return false;

        if (!attacker.level().isClientSide) {
            target.invulnerableTime = 0;
            attack(target, attacker, ABSOLUTE_DESTRUCTION_DAMAGE);
        }
        return true;
    }

    // 右键：发射独立弹射物
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            TheLastSwordYouNeverForgotProjectile.shoot(world, player, RandomSource.create(), ABSOLUTE_DESTRUCTION_DAMAGE);
        }
        return InteractionResultHolder.success(itemstack);
    }

    // 工具提示：Awesome等级（彩虹色）
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // Level: Awesome（彩虹色）
        MutableComponent levelLine = Component.translatable("item_tooltip.the_last_sword.level")
                .append(" ");
        levelLine.append(rainbowAwesome());
        tooltip.add(levelLine);

        // Lore
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_last_sword_you_never_forgot")
                .withStyle(ChatFormatting.GRAY));
    }

    // 生成彩虹色 "Awesome"
    private static Component rainbowAwesome() {
        String text = "Awesome";
        MutableComponent result = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            float hue = i / (float) text.length();
            int rgb = hsvToRgb(hue, 1f, 1f);
            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
        }
        return result;
    }

    private static int hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);
        return switch (h % 6) {
            case 0 -> rgb(value, t, p);
            case 1 -> rgb(q, value, p);
            case 2 -> rgb(p, value, t);
            case 3 -> rgb(p, q, value);
            case 4 -> rgb(t, p, value);
            default -> rgb(value, p, q);
        };
    }

    private static int rgb(float r, float g, float b) {
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    // 自定义攻击逻辑：记录血量 → 异常/致死则斩杀，正常则直接改血
    public static void attack(LivingEntity target, Entity attacker, float damage) {
        float originalHealth = target.getHealth();

        // 异常血量直接斩杀
        if (Float.isNaN(originalHealth) || Float.isInfinite(originalHealth) || originalHealth <= 0.0F) {
            EntityUtil.theLastEndSetDead(target, AbsoluteDestructionDamageSource.absoluteDestruction(attacker));
            return;
        }

        float expectedHealth = originalHealth - damage;
        // 致死伤害走斩杀，避免ECA处理极端负血量导致卡服
        if (expectedHealth <= 0) {
            EntityUtil.theLastEndSetDead(target, AbsoluteDestructionDamageSource.absoluteDestruction(attacker));
            return;
        }

        EntityUtil.theLastEndSetHealth(target, expectedHealth);
    }

    // 持有武器时添加ECA无敌、飞行、防御、免疫、无冷却，放下时全部移除
    @Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID)
    public static class HiddenSwordTickHandler {
        private static final String INVUL_TAG = "TheLastSwordYouNeverForgotInvul";
        private static final String FLY_TAG = "TheLastSwordYouNeverForgotFly";
        private static final String DEFENCE_TAG = "TheLastSwordYouNeverForgotDefence";

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            if (event.player.level().isClientSide) return;

            Player player = event.player;
            boolean hasSword = hasHiddenSword(player);

            if (hasSword) {
                // ECA无敌
                if (!EcaAPI.isInvulnerable(player)) {
                    EcaAPI.setInvulnerable(player, true);
                }
                player.getPersistentData().putBoolean(INVUL_TAG, true);

                // 飞行
                if (!player.isCreative() && !player.isSpectator()) {
                    if (TheLastSwordConfiguration.getAllowFlyingSafely() && !player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = true;
                        player.getPersistentData().putBoolean(FLY_TAG, true);
                        player.onUpdateAbilities();
                    }
                }

                // 设置保护标记（触发mixin防踢/防清除/防实体移除，不设world anchor避免与ECA无敌重复）
                EntityUtil.setProtection(player, true);
                player.getPersistentData().putBoolean(DEFENCE_TAG, true);

                // 免疫
                EntityUtil.applyImmunity(player);

                // 无冷却
                for (ItemStack stack : player.getInventory().items) {
                    if (!stack.isEmpty()) {
                        player.getCooldowns().removeCooldown(stack.getItem());
                    }
                }
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    player.getCooldowns().removeCooldown(offhand.getItem());
                }
            } else {
                // 移除ECA无敌
                if (player.getPersistentData().getBoolean(INVUL_TAG)) {
                    EcaAPI.setInvulnerable(player, false);
                    player.getPersistentData().remove(INVUL_TAG);
                }

                // 移除飞行
                if (player.getPersistentData().getBoolean(FLY_TAG)) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.getPersistentData().remove(FLY_TAG);
                    player.getPersistentData().remove("PlayerFlightIntent");
                    player.onUpdateAbilities();
                }

                // 移除保护标记
                if (player.getPersistentData().getBoolean(DEFENCE_TAG)) {
                    EntityUtil.setProtection(player, false);
                    player.getPersistentData().remove(DEFENCE_TAG);
                }
            }
        }

        private static boolean hasHiddenSword(Player player) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof TheLastSwordYouNeverForgot) return true;
            }
            return player.getOffhandItem().getItem() instanceof TheLastSwordYouNeverForgot;
        }
    }
}
