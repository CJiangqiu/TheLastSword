package net.the_last_sword.compat.lucky_block.lucky_event;

//幸运事件基类 - 子类声明闭区间 [minLuck, maxLuck], luck 落入区间即进入可抽取池
public abstract class LuckyEvent {

    public abstract LuckyEventCategory getCategory();

    public abstract int getMinLuck();

    public abstract int getMaxLuck();

    public abstract void execute(LuckyEventContext ctx);

    public final boolean isEligible(int luck) {
        return luck >= getMinLuck() && luck <= getMaxLuck();
    }
}
