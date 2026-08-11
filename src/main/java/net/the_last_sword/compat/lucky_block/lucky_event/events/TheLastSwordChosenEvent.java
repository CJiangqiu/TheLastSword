package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.init.ModItems;

//终焉之剑命定事件 - 玩家前 3 格地表启动 10 秒仪式: 中心哭泣黑曜石, 俯视顺时针 8 格黑曜石, 4 对角下界合金, 雷击后悬浮生成最终之剑
public class TheLastSwordChosenEvent extends LuckyEvent {

    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.chosen";
    private static final int TICKS_PER_SECOND = 20;

    //俯视顺时针 8 方向 (dx, dz), 索引 0 对应正北
    private static final int[][] CW8 = {
        { 0, -1},
        { 1, -1},
        { 1,  0},
        { 1,  1},
        { 0,  1},
        {-1,  1},
        {-1,  0},
        {-1, -1}
    };

    //4 对角偏移 (NW, NE, SW, SE)
    private static final int[][] CORNERS = {
        {-1, -1},
        { 1, -1},
        {-1,  1},
        { 1,  1}
    };

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.GOOD;
    }

    @Override
    public String getId() {
        return "The Last Sword";
    }

    @Override
    public int getMinLuck() {
        return 0;
    }

    @Override
    public int getMaxLuck() {
        return 100;
    }

    @Override
    public void execute(LuckyEventContext ctx) {
        Player player = ctx.player();
        if (player == null) return;
        ServerLevel world = ctx.world();
        BlockPos center = player.blockPosition().relative(player.getDirection(), 3).below();

        player.sendSystemMessage(Component.translatable(MESSAGE_KEY, player.getDisplayName()));
        setBlock(world, center, Blocks.CRYING_OBSIDIAN.defaultBlockState());

        int startIdx = directionIndex(player.getDirection());

        for (int i = 0; i < 8; i++) {
            int[] offset = CW8[(startIdx + i) % 8];
            BlockPos pos = center.offset(offset[0], 0, offset[1]);
            int delay = (i + 1) * TICKS_PER_SECOND;
            TheLastSwordMod.queueServerWork(delay, () -> setBlock(world, pos, Blocks.OBSIDIAN.defaultBlockState()));
        }

        TheLastSwordMod.queueServerWork(9 * TICKS_PER_SECOND, () -> {
            for (int[] offset : CORNERS) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                setBlock(world, pos, Blocks.NETHERITE_BLOCK.defaultBlockState());
            }
        });

        TheLastSwordMod.queueServerWork(10 * TICKS_PER_SECOND, () -> summonFinale(world, center));
    }

    private static void setBlock(ServerLevel world, BlockPos pos, BlockState state) {
        world.setBlockAndUpdate(pos, state);
    }

    private static void summonFinale(ServerLevel world, BlockPos center) {
        Vec3 centerVec = Vec3.atCenterOf(center);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt != null) {
            bolt.moveTo(centerVec.x, centerVec.y, centerVec.z);
            world.addFreshEntity(bolt);
        }
        world.playSound(null, center, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.0F, 1.0F);
        ItemEntity item = new ItemEntity(world, centerVec.x, centerVec.y + 1.5, centerVec.z, new ItemStack(ModItems.THE_LAST_SWORD.get()));
        item.setDeltaMovement(Vec3.ZERO);
        item.setNoGravity(true);
        item.setUnlimitedLifetime();
        world.addFreshEntity(item);
    }

    private static int directionIndex(Direction dir) {
        return switch (dir) {
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 6;
            default -> 0;
        };
    }
}
