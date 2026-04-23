package net.the_last_sword.compat.lucky_block.lucky_event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventCategory;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.init.ModItems;

import java.util.List;
import java.util.function.Supplier;

//宝石雨事件 - 以幸运方块位置为锚, 其上方 16 格处 8x8 平面每格每秒随机生成 1 件宝石, 持续 12 秒; 玩家仅用于消息播报
public class GemRainEvent extends LuckyEvent {

    private static final String MESSAGE_KEY = "message.the_last_sword.lucky_block.gem_rain";
    private static final int TICKS_PER_SECOND = 20;
    private static final int DURATION_SECONDS = 12;
    private static final int SKY_OFFSET = 16;
    private static final int PLANE_MIN = -4;
    private static final int PLANE_MAX = 3;

    private static final List<Supplier<Item>> GEM_POOL = List.of(
        () -> Items.REDSTONE,
        () -> Items.LAPIS_LAZULI,
        () -> Items.EMERALD,
        () -> Items.DIAMOND,
        () -> ModItems.DRAGON_CRYSTAL.get()
    );

    @Override
    public LuckyEventCategory getCategory() {
        return LuckyEventCategory.GOOD;
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
        ServerLevel world = ctx.world();
        BlockPos anchor = ctx.pos();
        Player player = ctx.player();
        if (player != null) {
            player.sendSystemMessage(Component.translatable(MESSAGE_KEY));
        }

        for (int second = 1; second <= DURATION_SECONDS; second++) {
            int delay = second * TICKS_PER_SECOND;
            TheLastSwordMod.queueServerWork(delay, () -> rainOneWave(world, anchor));
        }
    }

    private static void rainOneWave(ServerLevel world, BlockPos anchor) {
        RandomSource random = world.getRandom();
        double baseX = anchor.getX() + 0.5;
        double baseY = anchor.getY() + SKY_OFFSET + 0.5;
        double baseZ = anchor.getZ() + 0.5;
        for (int dx = PLANE_MIN; dx <= PLANE_MAX; dx++) {
            for (int dz = PLANE_MIN; dz <= PLANE_MAX; dz++) {
                Item gem = GEM_POOL.get(random.nextInt(GEM_POOL.size())).get();
                ItemEntity entity = new ItemEntity(world, baseX + dx, baseY, baseZ + dz, new ItemStack(gem));
                entity.setDeltaMovement(Vec3.ZERO);
                world.addFreshEntity(entity);
            }
        }
    }
}
