package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;

//爆炸事件，在方块位置触发一次的爆炸，破坏方块不点火
public class ExplosionEvent extends LuckyEvent {

    private static final float STRENGTH = 6.0F;
    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.explosion";

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.BAD;
    }

    @Override
    public String getId() {
        return "Explosion";
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
        BlockPos pos = ctx.pos();
        world.explode(
            null,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            STRENGTH,
            false,
            Level.ExplosionInteraction.BLOCK
        );
        if (ctx.player() != null) {
            ctx.player().sendSystemMessage(Component.translatable(MESSAGE_KEY));
        }
    }
}
