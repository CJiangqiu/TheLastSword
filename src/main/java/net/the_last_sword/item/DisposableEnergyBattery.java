package net.the_last_sword.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.nbt.ItemEnergyStorage;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.List;

//一次性能量电池：使用后消耗自身，立即为背包内所有FE未满的物品恢复能量
public class DisposableEnergyBattery extends Item {

    public DisposableEnergyBattery() {
        super(new Item.Properties()
            .stacksTo(64)
            .fireResistant()
            .rarity(Rarity.UNCOMMON)
        );
    }

    //最大能量 = 恢复数值（统一配置）
    public static int getMaxEnergy() {
        return TheLastSwordConfiguration.getDisposableEnergyBatteryRestoreAmountSafely();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemEnergyStorage energyStorage = new ItemEnergyStorage(stack, DisposableEnergyBattery::getMaxEnergy, true);
            private final LazyOptional<ItemEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                if (cap == ForgeCapabilities.ENERGY) {
                    return energyCap.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    //默认满电
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> energy.receiveEnergy(getMaxEnergy(), false));
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int restoreAmount = TheLastSwordConfiguration.getDisposableEnergyBatteryRestoreAmountSafely();
        boolean anyCharged = false;

        //为背包内所有FE未满的物品恢复能量
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack target = player.getInventory().getItem(i);
            if (target == stack || target.isEmpty()) {
                continue;
            }
            boolean charged = target.getCapability(ForgeCapabilities.ENERGY).map(energy -> {
                if (!energy.canReceive()) {
                    return false;
                }
                return energy.receiveEnergy(restoreAmount, false) > 0;
            }).orElse(false);
            if (charged) {
                anyCharged = true;
            }
        }

        //饰品栏（所有Curios槽位）
        anyCharged |= CuriosApi.getCuriosInventory(player).map(handler -> {
            boolean curioCharged = false;
            for (var entry : handler.getCurios().entrySet()) {
                IDynamicStackHandler stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack curioStack = stacks.getStackInSlot(i);
                    if (curioStack.isEmpty()) continue;
                    boolean charged = curioStack.getCapability(ForgeCapabilities.ENERGY).map(energy -> {
                        if (!energy.canReceive()) return false;
                        return energy.receiveEnergy(restoreAmount, false) > 0;
                    }).orElse(false);
                    if (charged) curioCharged = true;
                }
            }
            return curioCharged;
        }).orElse(false);

        //仅在确实充能后才消耗电池
        if (anyCharged) {
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> {
                int maxEnergy = energy.getMaxEnergyStored();
                if (maxEnergy == 0) return 0;
                return Math.round(13.0F * energy.getEnergyStored() / maxEnergy);
            })
            .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> {
                int maxEnergy = energy.getMaxEnergyStored();
                if (maxEnergy == 0) return 0x9B30FF;
                float ratio = (float) energy.getEnergyStored() / maxEnergy;
                if (ratio < 0.25F) {
                    return 0xFF0000;
                } else if (ratio < 0.5F) {
                    return 0xFF8C00;
                } else if (ratio < 0.75F) {
                    return 0x9B30FF;
                } else {
                    return 0xBF00FF;
                }
            })
            .orElse(0x9B30FF);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy ->
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.energy")
                .append(": §a" + energy.getEnergyStored() + " §r/ " + energy.getMaxEnergyStored() + " FE")));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.disposable_energy_battery",
            TheLastSwordConfiguration.getDisposableEnergyBatteryRestoreAmountSafely()));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.disposable_energy_battery")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
