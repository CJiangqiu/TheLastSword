package net.thelastsword.recipe;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thelastsword.capability.SwordCapability;
import net.thelastsword.configuration.TheLastSwordConfiguration;
import net.thelastsword.init.TheLastSwordModItems;
import net.thelastsword.world.inventory.DragonCrystalSmithingTableGuiMenu;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class UpgradeRecipe {
    private static ItemStack previousOutputItem = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            execute(event, event.player);
        }
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    public static void execute(@Nullable Event event, Entity entity) {
        if (entity == null)
            return;
        if (entity instanceof Player _plr0 && _plr0.containerMenu instanceof DragonCrystalSmithingTableGuiMenu) {
            ItemStack inputItem = (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt ? ((Slot) _slt.get(1)).getItem() : ItemStack.EMPTY);
            ItemStack templateItem = (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt ? ((Slot) _slt.get(0)).getItem() : ItemStack.EMPTY);
            ItemStack eggItem = (entity instanceof Player _plrSlotItem && _plrSlotItem.containerMenu instanceof Supplier _splr && _splr.get() instanceof Map _slt ? ((Slot) _slt.get(2)).getItem() : ItemStack.EMPTY);

            if (!inputItem.isEmpty() && !templateItem.isEmpty() && !eggItem.isEmpty()
                    && templateItem.getItem() == TheLastSwordModItems.DRAGON_CRYSTAL_UPGRADE_TEMPLATE.get()
                    && eggItem.getItem() == Blocks.DRAGON_EGG.asItem()) {

                inputItem.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(swordLevel -> {
                    ItemStack _setstack;
                    int level = swordLevel.getLevel();

                    if (inputItem.getItem() == TheLastSwordModItems.DRAGON_CRYSTAL_SWORD.get()) {
                        if (level < 5) {
                            _setstack = new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL_SWORD.get()).copy();
                            _setstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(newSwordLevel -> {
                                newSwordLevel.setLevel(level + 1);
                            });
                        } else {
                            _setstack = new ItemStack(TheLastSwordModItems.DRAGON_SWORD.get()).copy();
                            _setstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(newSwordLevel -> {
                                newSwordLevel.setLevel(6); // 设置龙之剑的默认等级为6
                            });
                        }
                    } else if (inputItem.getItem() == TheLastSwordModItems.DRAGON_SWORD.get()) {
                        if (level < 12) {
                            _setstack = new ItemStack(TheLastSwordModItems.DRAGON_SWORD.get()).copy();
                            _setstack.getCapability(SwordCapability.SWORD_LEVEL_CAPABILITY).ifPresent(newSwordLevel -> {
                                newSwordLevel.setLevel(level + 1);
                            });
                        } else {
                            _setstack = new ItemStack(TheLastSwordModItems.THE_LAST_SWORD.get()).copy();
                        }
                    } else {
                        return;
                    }

                    _setstack.getCapability(SwordCapability.EXTRA_ATTACK_DAMAGE_CAPABILITY).ifPresent(extraAttackDamage -> {
                        double increaseValue = TheLastSwordConfiguration.INCREASE_VALUE.get();
                        double increaseValueHighLevel = TheLastSwordConfiguration.INCREASE_VALUE_HIGH_LEVEL.get();
                        double extraDamage = (level < 6 ? increaseValue : increaseValueHighLevel) * (level + 1);
                        extraAttackDamage.setExtraAttackDamage((float) extraDamage);
                    });

                    _setstack.setTag(inputItem.getTag());
                    EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(inputItem), _setstack);
                    _setstack.setCount(1);
                    if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
                        ((Slot) _slots.get(3)).set(_setstack);
                        _player.containerMenu.broadcastChanges();
                    }
                });
            } else {
                if (entity instanceof Player _player && _player.containerMenu instanceof Supplier _current && _current.get() instanceof Map _slots) {
                    ((Slot) _slots.get(3)).set(ItemStack.EMPTY);
                    _player.containerMenu.broadcastChanges();
                }
            }


        }
    }
}
