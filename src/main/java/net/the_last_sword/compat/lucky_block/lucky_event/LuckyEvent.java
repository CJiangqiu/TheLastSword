package net.the_last_sword.compat.lucky_block.lucky_event;

import net.the_last_sword.configuration.TheLastSwordConfiguration;

//幸运事件基类 - 子类声明闭区间 [minLuck, maxLuck], luck 落入区间且权重大于 0 即进入可抽取池
public abstract class LuckyEvent {

    public abstract LuckyEventCategory getCategory();

    //配置键名, 对应配置文件 Compat Mods.Lucky Block 下的 "<id> Weight"
    public abstract String getId();

    public abstract int getMinLuck();

    public abstract int getMaxLuck();

    public abstract void execute(LuckyEventContext ctx);

    public final boolean isEligible(int luck) {
        return luck >= getMinLuck() && luck <= getMaxLuck();
    }

    //抽取权重, 0 表示禁用该事件
    public int getWeight() {
        return TheLastSwordConfiguration.getLuckyEventWeightSafely(getId());
    }
}
