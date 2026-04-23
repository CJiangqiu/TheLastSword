package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.init.ModEntities;

//封印事件 - 以幸运方块为 XZ 中心, 地表上方生成半径 5 / 高 5 的无顶铁栏杆围墙, 内部放入剑士/狂战士/弓箭手; 玩家非空则发消息并上虚弱/缓慢/失明 V 60 秒
public class VoidBountyEvent extends LuckyEvent {

    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.void_bounty";
    private static final int WALL_RADIUS = 5;
    private static final int WALL_HEIGHT = 5;
    private static final int DEBUFF_AMPLIFIER = 4;
    private static final int DEBUFF_DURATION_TICKS = 60 * 20;

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.BAD;
    }

    @Override
    public int getMinLuck() {
        return -100;
    }

    @Override
    public int getMaxLuck() {
        return 0;
    }

    @Override
    public void execute(LuckyEventContext ctx) {
        ServerLevel world = ctx.world();
        BlockPos center = ctx.pos();

        buildCell(world, center);
        spawnGuardian(world, ModEntities.GUARDIAN_SABER.get().create(world), center.offset(0, 0, 0));
        spawnGuardian(world, ModEntities.GUARDIAN_BERSERKER.get().create(world), center.offset(-2, 0, 0));
        spawnGuardian(world, ModEntities.GUARDIAN_ARCHER.get().create(world), center.offset(2, 0, 0));

        Player player = ctx.player();
        if (player == null) return;
        player.sendSystemMessage(Component.translatable(MESSAGE_KEY, player.getDisplayName()));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION_TICKS, DEBUFF_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION_TICKS, DEBUFF_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DEBUFF_DURATION_TICKS, DEBUFF_AMPLIFIER));
    }

    private static void buildCell(ServerLevel world, BlockPos center) {
        BlockState wall = Blocks.IRON_BARS.defaultBlockState();
        BlockState inside = Blocks.AIR.defaultBlockState();
        for (int dy = 0; dy < WALL_HEIGHT; dy++) {
            for (int dx = -WALL_RADIUS; dx <= WALL_RADIUS; dx++) {
                for (int dz = -WALL_RADIUS; dz <= WALL_RADIUS; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean isWall = Math.abs(dx) == WALL_RADIUS || Math.abs(dz) == WALL_RADIUS;
                    world.setBlockAndUpdate(pos, isWall ? wall : inside);
                }
            }
        }
    }

    private static void spawnGuardian(ServerLevel world, Mob entity, BlockPos pos) {
        if (entity == null) return;
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        ForgeEventFactory.onFinalizeSpawn(entity, world, world.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        world.addFreshEntity(entity);
    }
}
