package net.the_last_sword.util.health;

// 只镜像同步计时的零值状态；未知状态必须继续走权威读取。
public interface HealBanTickState {
    boolean the_last_sword$isHealBanTimeKnownZero();

    void the_last_sword$setHealBanTimeKnownZero(boolean knownZero);
}
