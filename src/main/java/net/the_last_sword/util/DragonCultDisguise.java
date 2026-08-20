package net.the_last_sword.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.init.ModTags;

import java.util.function.Predicate;

//拜龙教装束伪装判定
public final class DragonCultDisguise {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    //拜龙教目标筛选：跳过创造、旁观以及穿着整套拜龙教装束的玩家
    public static final Predicate<LivingEntity> NOT_DISGUISED_PLAYER =
            entity -> entity instanceof Player player
                    && !player.isCreative()
                    && !player.isSpectator()
                    && !isDisguised(player);

    private DragonCultDisguise() {
    }

    //四个盔甲槽全部命中拜龙教装束标签才算伪装成立
    public static boolean isDisguised(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!entity.getItemBySlot(slot).is(ModTags.DRAGON_CULT_DISGUISE)) {
                return false;
            }
        }
        return true;
    }
}
