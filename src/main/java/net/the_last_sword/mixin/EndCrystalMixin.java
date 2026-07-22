package net.the_last_sword.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.item.DragonArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Mixin(EndCrystal.class)
public class EndCrystalMixin {

    //在末影水晶tick时处理充能逻辑
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        EndCrystal crystal = (EndCrystal) (Object) this;

        if (crystal.level().isClientSide()) return;

        //检测周围配置范围内的玩家
        int range = TheLastSwordConfiguration.getDragonArmorEnderCrystalRangeSafely();
        AABB searchBox = new AABB(
                crystal.getX() - range, crystal.getY() - range, crystal.getZ() - range,
                crystal.getX() + range, crystal.getY() + range, crystal.getZ() + range
        );

        List<Player> nearbyPlayers = crystal.level().getEntitiesOfClass(Player.class, searchBox);

        //查找穿戴龙套且有物品需要充能的玩家
        Player targetPlayer = null;
        for (Player player : nearbyPlayers) {
            //1. 先检查玩家是否穿戴龙之套装
            if (hasDragonArmor(player)) {
                //2. 再检查玩家是否有能量未满的物品
                if (hasItemsNeedCharge(player)) {
                    targetPlayer = player;
                    break;
                }
            }
        }

        //设置光束目标并充能
        if (targetPlayer != null) {
            crystal.setBeamTarget(targetPlayer.blockPosition().below());

            //每tick给玩家所有物品充能
            chargePlayerItems(targetPlayer);
        } else {
            //没有需要充能的玩家，清除光束
            if (crystal.getBeamTarget() != null) {
                crystal.setBeamTarget(null);
            }
        }
    }

    //检查玩家是否穿戴龙之套装
    private boolean hasDragonArmor(Player player) {
        //只检查装备栏
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof DragonArmorItem) {
                return true;
            }
        }
        return false;
    }

    //检查玩家是否有能量未满的物品
    private boolean hasItemsNeedCharge(Player player) {
        //检查装备栏
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                boolean needsCharge = stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(energy -> energy.getEnergyStored() < energy.getMaxEnergyStored())
                        .orElse(false);
                if (needsCharge) return true;
            }
        }

        //检查背包
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                boolean needsCharge = stack.getCapability(ForgeCapabilities.ENERGY)
                        .map(energy -> energy.getEnergyStored() < energy.getMaxEnergyStored())
                        .orElse(false);
                if (needsCharge) return true;
            }
        }

        //检查饰品栏
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            for (var entry : handler.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        boolean needsCharge = stack.getCapability(ForgeCapabilities.ENERGY)
                                .map(energy -> energy.getEnergyStored() < energy.getMaxEnergyStored())
                                .orElse(false);
                        if (needsCharge) return true;
                    }
                }
            }
            return false;
        }).orElse(false);
    }

    //给玩家所有带FE capability且能量未满的物品充能
    private void chargePlayerItems(Player player) {
        int chargeRate = TheLastSwordConfiguration.getDragonArmorEnderCrystalChargeRateSafely();

        //充能装备栏
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                    if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                        energy.receiveEnergy(chargeRate, false);
                    }
                });
            }
        }

        //充能背包
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                    if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                        energy.receiveEnergy(chargeRate, false);
                    }
                });
            }
        }

        //充能饰品栏
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (var entry : handler.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                            if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                                energy.receiveEnergy(chargeRate, false);
                            }
                        });
                    }
                }
            }
        });
    }
}
