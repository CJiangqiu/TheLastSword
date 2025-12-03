package net.the_last_sword.test;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.the_last_sword.attack.AbsoluteDestructionDamageSource;
import net.the_last_sword.attack.PowerfulRangeAttack;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.init.ModKeyMappings;
import net.the_last_sword.util.EntityUtil;
import net.the_last_sword.util.nbt.ItemModeHelper;

import java.util.List;

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

    //近战攻击：范围128格绝对毁灭伤害
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return super.onEntitySwing(stack, entity);
        }

        //初始化模式系统
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);

        int mode = ItemModeHelper.getMode(stack);
        if (mode == 0 || mode == 1) {
            Level world = player.level();
            AABB range = player.getBoundingBox().inflate(128);
            List<Entity> targets = world.getEntitiesOfClass(Entity.class, range,
                    e -> !e.equals(player) && !(e instanceof Player p && p.isCreative()));
            for (Entity target : targets) {
                if (target instanceof LivingEntity living) {
                    AbsoluteDestructionDamageSource.applyAbsoluteDestructionIntelligently(living, entity, stack, 100);
                }
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    //右键使用：设置死亡或强力范围攻击
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //初始化模式系统
        ItemModeHelper.initializeMode(stack, 0, MAX_MODES);

        int mode = ItemModeHelper.getMode(stack);
        if (!world.isClientSide && (mode == 0 || mode == 1)) {
            ServerLevel server = (ServerLevel) world;
            Vec3 center = player.position();
            DamageSource ds = AbsoluteDestructionDamageSource.absoluteDestruction(player, stack);

            if (player.isShiftKeyDown()) {
                //Shift + 右键：调用强力范围攻击
                PowerfulRangeAttack.execute(world, player, center);
            } else {
                //右键：范围128格设置死亡（仅LivingEntity）
                AABB kill = new AABB(center, center).inflate(128);
                server.getEntitiesOfClass(LivingEntity.class, kill,
                        e -> !e.equals(player) && !(e instanceof Player p && p.isCreative()))
                        .forEach(t -> EntityUtil.theLastEndSetDead(t, ds));
            }
        }
        return InteractionResultHolder.success(stack);
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

        tooltip.add(Component.translatable("item_tooltip.the_last_sword.ultra_test_sword"));
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
                return;
            }

            //管理防御等级：持有剑=2级，防御模式=3级
            manageDefenseLevel(sp);
        }
    }

    //管理持有究极测试剑的玩家的防护等级
    private static void manageDefenseLevel(ServerPlayer player) {
        int requiredLevel = getRequiredDefenseLevel(player);
        int currentLevel = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;

        if (requiredLevel > 0) {
            if (currentLevel == 0) {
                DefenceManager.register(player, requiredLevel);
            } else {
                if (isUltraTestSwordDrivenDefense(currentLevel)) {
                    if (currentLevel != requiredLevel) {
                        DefenceManager.register(player, requiredLevel);
                    }
                } else if (currentLevel < requiredLevel) {
                    DefenceManager.register(player, requiredLevel);
                }
            }
        } else {
            if (currentLevel > 0 && isUltraTestSwordDrivenDefense(currentLevel)) {
                DefenceManager.clear(player);
            }
        }
    }

    //判断防御等级是否由究极测试剑提供
    private static boolean isUltraTestSwordDrivenDefense(int level) {
        return level == 2 || level == 3;
    }

    //获取玩家应该拥有的防护等级：默认模式2级，防御模式3级
    private static int getRequiredDefenseLevel(Player player) {
        if (player == null || player.getInventory() == null) return 0;

        boolean hasDefenseMode = false;
        boolean hasAnyMode = false;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof UltraTestSwordItem) {
                hasAnyMode = true;
                //初始化模式系统
                ItemModeHelper.initializeMode(stack, 0, MAX_MODES);
                int mode = ItemModeHelper.getMode(stack);
                if (mode == 1) {
                    hasDefenseMode = true;
                    break;
                }
            }
        }

        if (hasDefenseMode) return 3;  //防御模式 = 等级3
        if (hasAnyMode) return 2;      //默认模式 = 等级2
        return 0;                      //无剑 = 无防护
    }

    //检查玩家是否持有防御模式的究极测试剑
    public static boolean hasDefenseSword(Entity entity) {
        if (!(entity instanceof Player player)) return false;
        if (player.getInventory() == null) return false;

        for (ItemStack st : player.getInventory().items) {
            if (st.getItem() instanceof UltraTestSwordItem) {
                //初始化模式系统
                ItemModeHelper.initializeMode(st, 0, MAX_MODES);
                int m = ItemModeHelper.getMode(st);
                if (m == 1) return true;
            }
        }
        return false;
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

    //玩家离线时清理究极测试剑提供的防御等级
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cleanupUltraTestSwordDefense(player);
        }
    }

    //玩家死亡时清理究极测试剑提供的防御等级
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cleanupUltraTestSwordDefense(player);
        }
    }

    //清理由究极测试剑提供的防御等级
    private static void cleanupUltraTestSwordDefense(ServerPlayer player) {
        int currentLevel = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
        if (isUltraTestSwordDrivenDefense(currentLevel)) {
            DefenceManager.clear(player);
        }
    }

    //全局tick检查：确保所有有防御等级的玩家都正确地拥有究极测试剑
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        //每5秒检查一次（100 ticks）
        if (server.getTickCount() % 100 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                try {
                    int currentLevel = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
                    if (isUltraTestSwordDrivenDefense(currentLevel)) {
                        int requiredLevel = getRequiredDefenseLevel(player);
                        if (requiredLevel == 0) {
                            DefenceManager.clear(player);
                        }
                    }
                } catch (Exception e) {
                    //防止单个玩家的错误影响整个tick循环
                }
            }
        }
    }
}
