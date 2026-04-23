package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;

import java.util.ArrayList;
import java.util.List;

//铁砧末日事件 - 以幸运方块位置为锚, 其上方 32 格处 16x16 范围每秒随机 1-6 个不重复位置掉落铁砧, 持续 13 秒; 玩家仅用于消息播报
public class AnvilApocalypseEvent extends LuckyEvent {

    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.anvil_apocalypse";
    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_SECONDS = 13;
    private static final int SKY_OFFSET = 32;
    private static final int PLANE_MIN = -8;
    private static final int PLANE_MAX = 7;
    private static final int MIN_ANVILS_PER_WAVE = 1;
    private static final int MAX_ANVILS_PER_WAVE = 6;

    private static final Block[] ANVIL_TYPES = {
        Blocks.ANVIL,
        Blocks.CHIPPED_ANVIL,
        Blocks.DAMAGED_ANVIL
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
        ServerLevel world = ctx.world();
        BlockPos anchor = ctx.pos();
        Player player = ctx.player();
        if (player != null) {
            player.sendSystemMessage(Component.translatable(MESSAGE_KEY));
        }

        for (int second = 1; second <= DURATION_SECONDS; second++) {
            int delay = second * TICKS_PER_SECOND;
            TheLastSwordMod.queueServerWork(delay, () -> dropWave(world, anchor));
        }
    }

    private static void dropWave(ServerLevel world, BlockPos anchor) {
        RandomSource random = world.getRandom();
        int baseX = anchor.getX();
        int baseY = anchor.getY() + SKY_OFFSET;
        int baseZ = anchor.getZ();

        List<BlockPos> positions = new ArrayList<>((PLANE_MAX - PLANE_MIN + 1) * (PLANE_MAX - PLANE_MIN + 1));
        for (int dx = PLANE_MIN; dx <= PLANE_MAX; dx++) {
            for (int dz = PLANE_MIN; dz <= PLANE_MAX; dz++) {
                positions.add(new BlockPos(baseX + dx, baseY, baseZ + dz));
            }
        }

        int count = MIN_ANVILS_PER_WAVE + random.nextInt(MAX_ANVILS_PER_WAVE - MIN_ANVILS_PER_WAVE + 1);
        for (int i = 0; i < count; i++) {
            int j = i + random.nextInt(positions.size() - i);
            BlockPos tmp = positions.get(i);
            positions.set(i, positions.get(j));
            positions.set(j, tmp);
            Block anvil = ANVIL_TYPES[random.nextInt(ANVIL_TYPES.length)];
            FallingBlockEntity entity = FallingBlockEntity.fall(world, positions.get(i), anvil.defaultBlockState());
            entity.setHurtsEntities(2.0F, 40);
        }
    }
}
