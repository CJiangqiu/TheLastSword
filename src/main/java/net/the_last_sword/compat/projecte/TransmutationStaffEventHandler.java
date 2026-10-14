package net.the_last_sword.compat.projecte;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.TheLastSwordMod;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TransmutationStaffEventHandler {
    private TransmutationStaffEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteraction(event, event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        handleInteraction(event, event.getTarget());
    }

    private static void handleInteraction(PlayerInteractEvent event, Entity target) {
        if (!TransmutationStaffBehavior.isStaff(event.getItemStack())) {
            return;
        }
        if (event.getEntity().getCooldowns().isOnCooldown(event.getItemStack().getItem())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }
        if (TransmutationStaffBehavior.isTermination(event.getItemStack())) {
            event.setCancellationResult(TransmutationStaffBehavior.use(
                event.getLevel(), event.getEntity(), event.getHand()).getResult());
            event.setCanceled(true);
            return;
        }
        if (!TransmutationStaffBehavior.isTransmutation(event.getItemStack())) {
            return;
        }
        // 在实体自身交互前处理，避免交易或骑乘吞掉嬗变操作；已取消的保护事件不会进入此处。
        event.setCancellationResult(TransmutationStaffActions.interactEntity(event.getEntity(), event.getHand(), target));
        event.setCanceled(true);
    }
}
