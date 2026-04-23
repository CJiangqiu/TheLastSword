package net.the_last_sword.compat.lucky_block.lucky_event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

//幸运事件执行上下文 - 封装触发位置、玩家、幸运值等信息
public record LuckyEventContext(
    ServerLevel world,
    BlockPos pos,
    @Nullable Player player,
    int luck,
    RandomSource random,
    boolean removedByRedstone
) {}
