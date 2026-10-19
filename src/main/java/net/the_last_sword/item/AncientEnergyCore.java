package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class AncientEnergyCore extends Item implements IDragonSmithingTemplate, ICurioItem {

    //充能开关状态NBT标签
    private static final String CHARGING_ENABLED_TAG = "the_last_sword.charging_enabled";

    public AncientEnergyCore() {
        super(new Item.Properties()
            .stacksTo(1) //能量物品不能堆叠
            .fireResistant()
            .rarity(Rarity.RARE)
        );
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemEnergyStorage energyStorage = new ItemEnergyStorage(stack, TheLastSwordConfiguration::getAncientEnergyCoreMaxEnergySafely);
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

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; //始终显示能量条
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> {
                    int maxEnergy = energy.getMaxEnergyStored();
                    int currentEnergy = energy.getEnergyStored();
                    if (maxEnergy == 0) return 0;
                    return Math.round(13.0F * currentEnergy / maxEnergy);
                })
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> {
                    int maxEnergy = energy.getMaxEnergyStored();
                    int currentEnergy = energy.getEnergyStored();
                    if (maxEnergy == 0) return 0x9B30FF;

                    float ratio = (float) currentEnergy / maxEnergy;

                    if (ratio < 0.25F) {
                        return 0xFF0000; //红色
                    } else if (ratio < 0.5F) {
                        return 0xFF8C00; //橙色
                    } else if (ratio < 0.75F) {
                        return 0x9B30FF; //紫色
                    } else {
                        return 0xBF00FF; //亮紫色
                    }
                })
                .orElse(0x9B30FF);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        //Shift+右键切换充能状态
        if (player.isShiftKeyDown()) {
            boolean currentState = isChargingEnabled(stack);
            setChargingEnabled(stack, !currentState);

            //发送消息给玩家
            if (!level.isClientSide) {
                String key = !currentState ?
                    "item_tooltip.the_last_sword.ancient_energy_core.charging_enabled" :
                    "item_tooltip.the_last_sword.ancient_energy_core.charging_disabled";
                player.displayClientMessage(Component.translatable(key), true);
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        performCharging(stack, level, entity);
    }

    //Curios槽位的tick
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        performCharging(stack, slotContext.entity().level(), slotContext.entity());
    }

    //执行充能逻辑（供inventoryTick和curioTick共用）
    private void performCharging(ItemStack stack, Level level, Entity entity) {
        //服务端执行充能逻辑
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        //检查充能是否启用
        if (!isChargingEnabled(stack)) {
            return;
        }

        //获取能量核心的能量
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(coreEnergy -> {
            if (coreEnergy.getEnergyStored() <= 0) {
                return;
            }

            int rate = TheLastSwordConfiguration.getAncientEnergyCoreChargeRateSafely();
            if (rate <= 0) {
                return;
            }

            List<ItemStack> targets = new ArrayList<>();
            for (ItemStack armorStack : player.getArmorSlots()) {
                targets.add(armorStack);
            }
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                targets.add(player.getInventory().getItem(i));
            }
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (var entry : handler.getCurios().entrySet()) {
                    IDynamicStackHandler stacks = entry.getValue().getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        targets.add(stacks.getStackInSlot(i));
                    }
                }
            });

            // 少量补电后继续分配本刻余额，重复出现的装备槽只处理一次。
            Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            visited.add(stack);
            int remaining = rate;
            for (ItemStack target : targets) {
                if (target.isEmpty() || !visited.add(target)) continue;
                remaining -= chargeItem(coreEnergy, target, remaining);
                if (remaining <= 0 || coreEnergy.getEnergyStored() <= 0) break;
            }
        });
    }

    private int chargeItem(IEnergyStorage source, ItemStack target, int limit) {
        if (target.isEmpty()) {
            return 0;
        }

        return target.getCapability(ForgeCapabilities.ENERGY).map(targetEnergy -> {
            if (targetEnergy == source || !targetEnergy.canReceive()
                    || targetEnergy.getEnergyStored() >= targetEnergy.getMaxEnergyStored()) {
                return 0;
            }

            //尝试提取能量
            int extracted = source.extractEnergy(limit, true);
            if (extracted <= 0) {
                return 0;
            }

            //尝试充入目标
            int received = targetEnergy.receiveEnergy(extracted, false);
            if (received > 0) {
                //从能量核心中实际扣除能量
                source.extractEnergy(received, false);
            }

            return Math.max(0, received);
        }).orElse(0);
    }

    //获取充能启用状态
    private boolean isChargingEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false; //默认关闭
        }
        return tag.getBoolean(CHARGING_ENABLED_TAG);
    }

    //设置充能启用状态
    private void setChargingEnabled(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(CHARGING_ENABLED_TAG, enabled);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        //显示能量信息
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.energy")
                    .append(": §a" + energy.getEnergyStored() + " §r/ " + energy.getMaxEnergyStored() + " FE"));
        });

        //显示充能功能说明和当前状态
        boolean chargingEnabled = isChargingEnabled(stack);
        String statusKey = chargingEnabled ?
            "item_tooltip.the_last_sword.ancient_energy_core.charging_enabled" :
            "item_tooltip.the_last_sword.ancient_energy_core.charging_disabled";
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.ancient_energy_core",
            TheLastSwordConfiguration.getAncientEnergyCoreChargeRateSafely(),
            Component.translatable(statusKey)));

        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.ancient_energy_core")
            .withStyle(ChatFormatting.GRAY));
    }
}
