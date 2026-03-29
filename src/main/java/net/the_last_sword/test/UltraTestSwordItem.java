package net.the_last_sword.test;

import net.eca.api.EcaAPI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.damagesource.AbsoluteDestructionDamageSource;
import net.the_last_sword.init.ModKeyMappings;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemModeHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "the_last_sword")
public class UltraTestSwordItem extends TieredItem {

    private static final int MAX_MODES = 2; // 0=强力范围攻击模式, 1=防御模式

    public UltraTestSwordItem() {
        super(new Tier() {
            @Override public int getUses() { return 0; }
            @Override public float getSpeed() { return 1024f; }
            @Override public float getAttackDamageBonus() { return 1022f; }
            @Override public int getLevel() { return 4; }
            @Override public int getEnchantmentValue() { return 1024; }
            @Override public Ingredient getRepairIngredient() { return Ingredient.of(); }
        }, new Item.Properties().fireResistant());
    }

    //近战攻击：左键绝毁，Shift+左键终焉死亡
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || player.level().isClientSide()) {
            return super.onEntitySwing(stack, entity);
        }

        //初始化模式系统
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);

        int mode = ItemModeHelper.getMode(stack);
        if (mode == 0 || mode == 1) {
            List<Entity> targets = selectTargetsWithEcaSelector(player);
            DamageSource damageSource = AbsoluteDestructionDamageSource.absoluteDestruction(player, stack);
            for (Entity target : targets) {
                if (target instanceof LivingEntity living) {
                    if (player.isShiftKeyDown()) {
                        EntityUtil.theLastEndSetDead(living, damageSource);
                    } else {
                        AbsoluteDestructionDamageSource.applyAbsoluteDestruction(living,player,stack,100);
                    }
                }
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    //右键使用：清除或最强攻击
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //初始化模式系统
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);

        int mode = ItemModeHelper.getMode(stack);
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer && (mode == 0 || mode == 1)) {
            if (serverPlayer.isShiftKeyDown()) {
                PowerfulRangeAttack.execute(serverPlayer);
            } else {
                List<Entity> targets = selectTargetsWithEcaSelector(serverPlayer);
                for (Entity target : targets) {
                    EntityUtil.theLastEndRemove(target, Entity.RemovalReason.KILLED);
                }
            }
        }
        return InteractionResultHolder.success(stack);
    }

    //使用ECA选择器获取128格目标实体
    private static List<Entity> selectTargetsWithEcaSelector(ServerPlayer sourcePlayer) {
        List<Entity> result = new ArrayList<>();
        AABB area = sourcePlayer.getBoundingBox().inflate(128);
        for (Entity entity : EcaAPI.getEntities(sourcePlayer.level(), area)) {
            if (entity == sourcePlayer) {
                continue;
            }
            if (entity instanceof Player targetPlayer && targetPlayer.isCreative()) {
                continue;
            }
            result.add(entity);
        }
        return result;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable("item_tooltip.the_last_sword.creative"));

        int mode = ItemModeHelper.getMode(stack);
        String modeKey = (mode == 0)
                ? "item_tooltip.the_last_sword.powerful_range_attack_mode"
                : "item_tooltip.the_last_sword.defense_mode";
        tooltip.add(
                Component.translatable("item_tooltip.the_last_sword.mode")
                        .append(" ")
                        .append(Component.translatable(modeKey))
        );

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.shift"));
        } else {
            String descrKey = (mode == 0)
                    ? "item_tooltip.the_last_sword.powerful_range_attack_mode_descr"
                    : "item_tooltip.the_last_sword.defense_mode_descr";
            tooltip.add(
                    Component.translatable(descrKey)
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)))
            );
        }

        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.ultra_test_sword")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(
                Component.translatable("item_tooltip.the_last_sword.mode_key")
                        .append(" ")
                        .append(ModKeyMappings.CHANGE_SWORD_MODE.getKey().getDisplayName().getString())
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        //初始化模式系统
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);

        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            boolean isCreativeOrSpec = sp.isCreative() || sp.isSpectator();
            boolean isOp = sp.hasPermissions(2);

            //仅允许（创造模式或旁观模式）且为管理员的玩家保留
            if (!(isCreativeOrSpec && isOp)) {
                sp.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    //检查玩家是否持有究极测试剑（主背包+副手）
    public static boolean hasUltraTestSword(Player player) {
        if (player == null || player.getInventory() == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof UltraTestSwordItem) {
                return true;
            }
        }
        return player.getOffhandItem().getItem() instanceof UltraTestSwordItem;
    }

    //检查玩家是否持有防御模式的究极测试剑（主背包+副手）
    public static boolean hasDefenseSword(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        if (player.getInventory() == null) return false;

        for (ItemStack st : player.getInventory().items) {
            if (st.getItem() instanceof UltraTestSwordItem) {
                ItemModeHelper.initializeMode(st, 0, MAX_MODES);
                if (ItemModeHelper.getMode(st) == 1) return true;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof UltraTestSwordItem) {
            ItemModeHelper.initializeMode(offhand, 0, MAX_MODES);
            if (ItemModeHelper.getMode(offhand) == 1) return true;
        }
        return false;
    }

    //玩家刻：防御管理
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer sp)) return;

        //权限检查：仅OP创造/旁观模式玩家
        boolean isCreativeOrSpec = sp.isCreative() || sp.isSpectator();
        boolean isOp = sp.hasPermissions(2);
        if (!(isCreativeOrSpec && isOp)) return;

        //TLS防御逻辑：持有究极测试剑时始终注册
        if (hasUltraTestSword(sp)) {
            EntityUtil.registerDefence(sp, sp.getMaxHealth());
            sp.getPersistentData().putBoolean("UltraTestSwordTLSDefence", true);
        } else {
            if (sp.getPersistentData().getBoolean("UltraTestSwordTLSDefence")) {
                EntityUtil.clearDefence(sp);
                sp.getPersistentData().remove("UltraTestSwordTLSDefence");
            }
        }

        //ECA无敌+位置锁定：仅防御模式
        if (hasDefenseSword(sp)) {
            if (!EcaAPI.isInvulnerable(sp)) {
                EcaAPI.setInvulnerable(sp, true);
            }
            EcaAPI.lockLocation(sp);
            sp.getPersistentData().putBoolean("UltraTestSwordDefence", true);
        } else {
            if (sp.getPersistentData().getBoolean("UltraTestSwordDefence")) {
                EcaAPI.setInvulnerable(sp, false);
                EcaAPI.unlockLocation(sp);
                sp.getPersistentData().remove("UltraTestSwordDefence");
            }
        }
    }

    //防止持有防御模式剑的玩家进行维度旅行
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof Player player && hasDefenseSword(player)) {
            event.setCanceled(true);
        }
    }

    //防止持有防御模式剑的玩家切换游戏模式
    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        if (hasDefenseSword(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
