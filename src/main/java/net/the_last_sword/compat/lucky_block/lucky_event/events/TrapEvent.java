package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;

//陷阱事件 - 以玩家为中心, 脚下 1 格岩浆, 脚部同层 8 格黑曜石, 黑曜石之上 8 个末影水晶; 玩家为空时跳过
public class TrapEvent extends LuckyEvent {

    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.trap";

    private static final int[][] HORIZONTAL_OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        { 0, -1},          { 0, 1},
        { 1, -1}, { 1, 0}, { 1, 1}
    };

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
        Player player = ctx.player();
        if (player == null) return;
        ServerLevel world = ctx.world();
        BlockPos center = player.blockPosition();
        world.setBlockAndUpdate(center.below(), Blocks.LAVA.defaultBlockState());
        for (int[] offset : HORIZONTAL_OFFSETS) {
            BlockPos obsidianPos = center.offset(offset[0], 0, offset[1]);
            BlockPos crystalPos = obsidianPos.above();
            world.setBlockAndUpdate(obsidianPos, Blocks.OBSIDIAN.defaultBlockState());
            EndCrystal crystal = new EndCrystal(world, crystalPos.getX() + 0.5, crystalPos.getY(), crystalPos.getZ() + 0.5);
            crystal.setShowBottom(false);
            world.addFreshEntity(crystal);
        }
        player.sendSystemMessage(Component.translatable(MESSAGE_KEY));
    }
}
