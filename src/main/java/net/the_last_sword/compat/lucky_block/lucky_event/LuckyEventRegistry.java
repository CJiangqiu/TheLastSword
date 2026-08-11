package net.the_last_sword.compat.lucky_block.lucky_event;

import net.minecraft.util.RandomSource;
import net.the_last_sword.compat.lucky_block.lucky_event.events.AnvilApocalypseEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.ArenaEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.DragonBobEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.DragonCrystalBobEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.EndResetSkyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.ExplosionEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.GemRainEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.LuckyDropEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.LuckyWellEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.TheLastSwordChosenEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.TrapEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.VoidBountyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.events.WoundOfTimeLaunchEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

//幸运事件注册表 - 懒加载静态列表, 按 luck 过滤可用事件后依配置权重抽取, 空池兜底返回 null
public final class LuckyEventRegistry {

    private static final List<LuckyEvent> EVENTS = new ArrayList<>();
    private static boolean bootstrapped = false;

    private LuckyEventRegistry() {}

    public static void register(LuckyEvent event) {
        EVENTS.add(event);
    }

    private static void bootstrapOnce() {
        if (bootstrapped) return;
        bootstrapped = true;
        register(new ExplosionEvent());
        register(new TrapEvent());
        register(new EndResetSkyEvent());
        register(new TheLastSwordChosenEvent());
        register(new GemRainEvent());
        register(new AnvilApocalypseEvent());
        register(new WoundOfTimeLaunchEvent());
        register(new DragonCrystalBobEvent());
        register(new DragonBobEvent());
        register(new VoidBountyEvent());
        register(new LuckyDropEvent());
        register(new LuckyWellEvent());
        register(new ArenaEvent());
    }

    //过滤全部可用事件, 按权重抽一个; 若全池为空, 返回 null 以触发外层兜底
    @Nullable
    public static LuckyEvent pickEvent(int luck, RandomSource random) {
        return pickWeighted(luck, random);
    }

    //同 pickEvent, 但排除指定事件类(井变体专用, 避免递归刷井等)
    @SafeVarargs
    @Nullable
    public static LuckyEvent pickEventExcluding(int luck, RandomSource random, Class<? extends LuckyEvent>... exclusions) {
        return pickWeighted(luck, random, exclusions);
    }

    //权重抽取 - 跳过 luck 区间外、被排除以及权重非正的事件, 按权重占比落点选中
    @SafeVarargs
    @Nullable
    private static LuckyEvent pickWeighted(int luck, RandomSource random, Class<? extends LuckyEvent>... exclusions) {
        bootstrapOnce();
        List<LuckyEvent> pool = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int total = 0;
        outer:
        for (LuckyEvent e : EVENTS) {
            if (!e.isEligible(luck)) continue;
            for (Class<? extends LuckyEvent> ex : exclusions) {
                if (ex.isInstance(e)) continue outer;
            }
            int weight = e.getWeight();
            if (weight <= 0) continue;
            pool.add(e);
            weights.add(weight);
            total += weight;
        }
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (int i = 0; i < pool.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) return pool.get(i);
        }
        return pool.get(pool.size() - 1);
    }
}
