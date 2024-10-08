package net.thelastsword.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.damagetype.AbsoluteDestruction;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.util.EntityUtil;

import java.util.List;

public class PowerfulRangeAttack {

    public static void performPowerfulRangeAttack(Level world, Player player, ItemStack itemstack) {
        double range = TheLastSwordConfiguration.RANGE_ATTACK_RANGE.get(); // 使用配置文件中的范围值

        // 使用 EntityUtil 工具类中的 EntityGoal 方法获取攻击目标
        List<Entity> entities = EntityUtil.EntityGoal(world, player, range);

        // 获取额外攻击伤害
        float extraAttackDamage = itemstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY)
                .map(cap -> cap.getExtraAttackDamage())
                .orElse(0.0f);

        // 获取绝对毁灭伤害倍数
        double destructionMultiplier = TheLastSwordConfiguration.ABSOLUTE_DESTRUCTION_MULTIPLIER.get();

        // 对所有实体进行攻击
        for (Entity entity : entities) {
            AbsoluteDestruction.applyAbsoluteDestruction(entity, extraAttackDamage * (float) destructionMultiplier); // 使用额外伤害值和倍数
        }

        // 添加冷却时间
        int cooldown = TheLastSwordConfiguration.RANGE_ATTACK_COOLDOWN.get() * 2;
        player.getCooldowns().addCooldown(itemstack.getItem(), cooldown);

        // 播放音效
        world.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
