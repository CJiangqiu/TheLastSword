package net.thelastsword.recipe;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thelastsword.capability.SwordCapability;

import java.util.function.Supplier;
import java.util.Map;

public class CleanSlot {
    public static void execute(Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ((Slot) _slots.get(0)).remove(1);
            _player.containerMenu.broadcastChanges();
        }
        if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ((Slot) _slots.get(1)).remove(1);
            _player.containerMenu.broadcastChanges();
        }
        if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
            ((Slot) _slots.get(2)).remove(1);
            _player.containerMenu.broadcastChanges();
        }

        // 播放声音
        ItemStack outputItem = (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt ? ((Slot) _slt.get(3)).getItem() : ItemStack.EMPTY);
        if (!outputItem.isEmpty()) {
            outputItem.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
                int level = swordLevel.getLevel();
                Level entityLevel = entity.getCommandSenderWorld();
                if (level >= 13) {
                    entityLevel.playSound(null, entity.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.0F);
                } else {
                    entityLevel.playSound(null, entity.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            });
        }
    }
}
