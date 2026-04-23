package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventRegistry;

import java.util.List;

//幸运井触发事件 - 井变体幸运方块专用; 不注册进 Registry; 在井幸运方块周围5x5x5找最近玩家后抽一个非"放井"事件执行, 最后在原位放信标
public class LuckyWellTriggerEvent extends LuckyEvent {

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.NEUTRAL;
    }

    @Override
    public int getMinLuck() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMaxLuck() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void execute(LuckyEventContext ctx) {
        ServerLevel world = ctx.world();
        Vec3 center = Vec3.atCenterOf(ctx.pos());
        //红石触发没有玩家, 在5x5x5范围内挑最近的活体玩家作为内层事件的触发者
        List<Player> candidates = world.getEntitiesOfClass(Player.class, AABB.ofSize(center, 5.0D, 5.0D, 5.0D), p -> p.isAlive() && !p.isSpectator());
        Player nearest = null;
        double minDistSq = Double.MAX_VALUE;
        for (Player p : candidates) {
            double distSq = p.distanceToSqr(center);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = p;
            }
        }
        LuckyEventContext inferredCtx = (nearest != null && ctx.player() == null)
            ? new LuckyEventContext(world, ctx.pos(), nearest, ctx.luck(), ctx.random(), ctx.removedByRedstone())
            : ctx;

        LuckyEvent inner = LuckyEventRegistry.pickEventExcluding(ctx.luck(), ctx.random(), LuckyWellEvent.class);
        if (inner != null) {
            inner.execute(inferredCtx);
        }
        world.setBlock(ctx.pos(), Blocks.BEACON.defaultBlockState(), 3);
    }
}
