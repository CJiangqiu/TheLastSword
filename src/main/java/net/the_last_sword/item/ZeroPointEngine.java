package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.nbt.ItemEnergyStorage;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class ZeroPointEngine extends Item implements ICurioItem {
    private static final String CHARGING_ENABLED_TAG = "the_last_sword.charging_enabled";

    public ZeroPointEngine() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() ->
                    new ItemEnergyStorage(stack, TheLastSwordConfiguration::getZeroPointEngineMaxEnergySafely));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged || oldStack.getItem() != newStack.getItem()
                || oldStack.getCount() != newStack.getCount()) return true;
        if (oldStack == newStack) return false;

        // 持续发电会同步新的物品数据，能量变化不应反复触发收起和抬手。
        CompoundTag oldTag = oldStack.hasTag() ? oldStack.getTag().copy() : new CompoundTag();
        CompoundTag newTag = newStack.hasTag() ? newStack.getTag().copy() : new CompoundTag();
        oldTag.remove("the_last_sword.energy");
        newTag.remove("the_last_sword.energy");
        return !oldTag.equals(newTag);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY).map(energy -> {
            int capacity = energy.getMaxEnergyStored();
            return capacity <= 0 ? 0 : Math.min(13, Math.round(13.0F * energy.getEnergyStored() / capacity));
        }).orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xBF00FF;
    }

    private boolean isChargingEnabled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(CHARGING_ENABLED_TAG);
    }

    private Component chargingStatus(ItemStack stack) {
        return Component.translatable("item_tooltip.the_last_sword.ancient_energy_core.charging_"
                + (isChargingEnabled(stack) ? "enabled" : "disabled"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            stack.getOrCreateTag().putBoolean(CHARGING_ENABLED_TAG, !isChargingEnabled(stack));
            player.displayClientMessage(chargingStatus(stack), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        tickEnergy(stack, entity);
    }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        tickEnergy(stack, context.entity());
    }

    private void tickEnergy(ItemStack stack, Entity entity) {
        if (entity.level().isClientSide || !(entity instanceof Player player)) return;
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(source -> {
            source.receiveEnergy(TheLastSwordConfiguration.getZeroPointEngineGenerationRateSafely(), false);
            if (!isChargingEnabled(stack) || source.getEnergyStored() <= 0) return;
            int rate = TheLastSwordConfiguration.getZeroPointEngineChargeRateSafely();
            if (rate <= 0) return;

            List<ItemStack> targets = new ArrayList<>();
            targets.add(player.getItemBySlot(EquipmentSlot.HEAD));
            targets.add(player.getItemBySlot(EquipmentSlot.CHEST));
            targets.add(player.getItemBySlot(EquipmentSlot.LEGS));
            targets.add(player.getItemBySlot(EquipmentSlot.FEET));
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                targets.add(player.getInventory().items.get(i));
            }
            targets.add(player.getOffhandItem());
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (var slot : handler.getCurios().values()) {
                    var stacks = slot.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) targets.add(stacks.getStackInSlot(i));
                }
            });

            // 多个槽位引用同一物品时只处理一次，成功充能后不再分配余下额度。
            Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(stack);
            for (ItemStack target : targets) {
                if (target.isEmpty() || !visited.add(target)) continue;
                if (chargeItem(source, target, rate) > 0) break;
            }
        });
    }

    private int chargeItem(IEnergyStorage source, ItemStack target, int limit) {
        return target.getCapability(ForgeCapabilities.ENERGY).map(receiver -> {
            if (receiver == source || !receiver.canReceive()) return 0;
            int available = source.extractEnergy(limit, true);
            if (available <= 0) return 0;
            int received = receiver.receiveEnergy(available, false);
            if (received > 0) source.extractEnergy(received, false);
            return Math.max(0, received);
        }).orElse(0);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy ->
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.energy")
                        .append(": §a" + energy.getEnergyStored() + " §r/ " + energy.getMaxEnergyStored() + " FE")));
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.zero_point_engine",
                TheLastSwordConfiguration.getZeroPointEngineGenerationRateSafely(),
                TheLastSwordConfiguration.getZeroPointEngineChargeRateSafely(), chargingStatus(stack)));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.zero_point_engine")
                .withStyle(ChatFormatting.GRAY));
    }
}
