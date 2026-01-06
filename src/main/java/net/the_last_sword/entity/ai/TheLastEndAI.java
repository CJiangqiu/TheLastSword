package net.the_last_sword.entity.ai;

import net.minecraft.world.phys.Vec3;
import net.the_last_sword.entity.TheLastEndEntity;

import java.util.ArrayList;
import java.util.List;

//终焉种族AI基类
public abstract class TheLastEndAI {
    protected final TheLastEndEntity entity;
    protected final List<Skill> skills = new ArrayList<>();
    protected Skill currentSkill = null;
    private boolean shutdown = false;

    public TheLastEndAI(TheLastEndEntity entity) {
        this.entity = entity;
        //子类注册技能
        registerSkills();
    }

    //子类覆写此方法注册技能
    protected abstract void registerSkills();

    //添加技能到列表
    protected void addSkill(Skill skill) {
        skills.add(skill);
    }

    //每tick更新
    public void update() {
        //AI已关闭，不执行任何逻辑
        if (shutdown) {
            return;
        }

        //如果有技能正在执行
        if (currentSkill != null) {
            int skillTick = entity.getSkillTick();

            //如果技能不允许移动，停止移动和动量
            if (!currentSkill.allowMovementDuringSkill()) {
                entity.getNavigation().stop();
                entity.setDeltaMovement(Vec3.ZERO);
            }

            //触发关键帧
            currentSkill.tick(skillTick);

            //检查技能是否结束
            if (skillTick >= currentSkill.getDuration()) {
                //技能结束，重置状态
                currentSkill = null;
                entity.setSkillTick(0);
                //不再自动重置动画，交由后续逻辑处理
            } else {
                //技能进行中，递增计时器
                entity.setSkillTick(skillTick + 1);
            }
        } else {
            //没有技能执行，重置到 idle（如果还不是 idle）
            String currentAnim = entity.getAnimation();
            String idleName = entity.getIdleAnimationName();
            if (!currentAnim.equals(idleName)) {
                entity.setAnimation(idleName);
            }
            //没有技能执行，调用AI决策
            aiTick();
        }
    }

    //子类覆写此方法实现AI决策逻辑
    protected abstract void aiTick();

    //使用指定技能
    protected void useSkill(Skill skill) {
        if (currentSkill == null) {
            currentSkill = skill;
            skill.use();
        }
    }

    //根据动画名称查找技能
    protected Skill getSkillByAnimation(String animationName) {
        for (Skill skill : skills) {
            if (skill.getAnimationName().equals(animationName)) {
                return skill;
            }
        }
        return null;
    }

    //获取当前正在执行的技能
    public Skill getCurrentSkill() {
        return currentSkill;
    }

    //检查是否有技能正在执行
    public boolean isUsingSkill() {
        return currentSkill != null;
    }

    //关闭AI（用于安全移除实体）
    public void shutdown() {
        this.shutdown = true;
        this.currentSkill = null;  //清除当前技能
        entity.getNavigation().stop();  //停止寻路
        entity.setTarget(null);  //清除目标
    }

    //检查AI是否已关闭
    public boolean isShutdown() {
        return shutdown;
    }
}
