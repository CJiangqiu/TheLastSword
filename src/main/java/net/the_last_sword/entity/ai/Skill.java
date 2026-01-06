package net.the_last_sword.entity.ai;

import net.the_last_sword.entity.TheLastEndEntity;

import java.util.HashMap;
import java.util.Map;

//技能基类
public abstract class Skill {
    protected final TheLastEndEntity entity;
    protected final String animationName;
    protected final int duration;
    protected final Map<Integer, Runnable> keyframes = new HashMap<>();

    public Skill(TheLastEndEntity entity, String animationName, int duration) {
        this.entity = entity;
        this.animationName = animationName;
        this.duration = duration;

        //子类定义关键帧
        defineKeyframes();
    }

    //子类覆写此方法定义关键帧
    protected abstract void defineKeyframes();

    //子类调用此方法添加关键帧
    protected void addKeyframe(int tick, Runnable action) {
        keyframes.put(tick, action);
    }

    //启动技能
    public void use() {
        entity.setAnimation(animationName);
        entity.setSkillTick(0);
    }

    //每tick更新，触发关键帧
    public void tick(int currentTick) {
        Runnable action = keyframes.get(currentTick);
        if (action != null) {
            action.run();
        }
    }

    //获取技能动画名称
    public String getAnimationName() {
        return animationName;
    }

    //获取技能时长
    public int getDuration() {
        return duration;
    }

    //技能期间是否允许移动（子类可覆写）
    public boolean allowMovementDuringSkill() {
        return false;  // 默认不允许移动
    }
}
