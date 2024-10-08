package net.thelastsword.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.damagetype.AbsoluteDestruction;

@Mod.EventBusSubscriber
public class JustifiedDefense {

    public static void applyJustifiedDefense(Entity entity) {
        CompoundTag nbt = entity.getPersistentData();
        nbt.putBoolean("justified_defense", true);
    }

    public static boolean hasJustifiedDefense(Entity entity) {
        CompoundTag nbt = entity.getPersistentData();
        return nbt.getBoolean("justified_defense");
    }

    public static void removeJustifiedDefense(Entity entity) {
        CompoundTag nbt = entity.getPersistentData();
        nbt.remove("justified_defense");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (hasJustifiedDefense(entity)) {
            if (event.getSource() == AbsoluteDestruction.ABSOLUTE_DESTRUCTION) {
                removeJustifiedDefense(entity);
            }
            event.setCanceled(true); // 取消受伤事件
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (hasJustifiedDefense(entity)) {
            event.setCanceled(true); // 取消死亡事件
        }
    }
}
