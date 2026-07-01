package net.the_last_sword.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModAttributes;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//玩家相关注入
@Mixin(Player.class)
public class PlayerMixin {

    //防止外部代码替换受保护玩家的装备槽（受强力背包保护开关控制）
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void tls$blockSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (TheLastSwordConfiguration.getEnableStrongInventoryProtectionSafely()
                && EntityUtil.hasInventoryProtection((Player) (Object) this)
                && net.eca.util.EntityUtil.hasExternalCaller(5)) {
            ci.cancel();
        }
    }

    //拦截死亡时清空背包（keepInventory=false 时调用，取消后物品不掉落不消失）
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void tls$blockDropEquipment(CallbackInfo ci) {
        if (EntityUtil.hasInventoryProtection((Player) (Object) this)) {
            ci.cancel();
        }
    }

    //拦截外部mod生成掉落物（防止强制缴械），玩家自己丢物品不受影响
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void tls$blockDrop(ItemStack stack, boolean b1, boolean b2, CallbackInfoReturnable<ItemEntity> cir) {
        if (EntityUtil.hasInventoryProtection((Player) (Object) this)
                && net.eca.util.EntityUtil.hasExternalCaller(5)) {
            cir.setReturnValue(null);
        }
    }

    // 持久化世界锚度血量到磁盘
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void tls$saveWorldAnchor(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        CompoundTag pd = player.getPersistentData();
        if (pd.contains("tlsWorldAnchor")) {
            tag.putString("tlsWorldAnchor", pd.getString("tlsWorldAnchor"));
        }
        if (pd.contains("tlsWorldAnchorMax")) {
            tag.putString("tlsWorldAnchorMax", pd.getString("tlsWorldAnchorMax"));
        }
        if (pd.contains("tlsIsProtected")) {
            tag.putBoolean("tlsIsProtected", pd.getBoolean("tlsIsProtected"));
        }
        // 持久化肃正防御值
        AttributeInstance shield = player.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
        if (shield != null && shield.getValue() > 0) {
            tag.putDouble("tlsJustifiedDefence", shield.getValue());
        }
        AttributeInstance maxShield = player.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
        if (maxShield != null && maxShield.getValue() > 0) {
            tag.putDouble("tlsMaxJustifiedDefence", maxShield.getValue());
        }
    }

    // 从磁盘恢复世界锚度血量
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tls$loadWorldAnchor(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        CompoundTag pd = player.getPersistentData();
        if (tag.contains("tlsWorldAnchor")) {
            pd.putString("tlsWorldAnchor", tag.getString("tlsWorldAnchor"));
        }
        if (tag.contains("tlsWorldAnchorMax")) {
            pd.putString("tlsWorldAnchorMax", tag.getString("tlsWorldAnchorMax"));
        }
        if (tag.contains("tlsIsProtected")) {
            pd.putBoolean("tlsIsProtected", tag.getBoolean("tlsIsProtected"));
        }
        // 恢复肃正防御值
        if (tag.contains("tlsJustifiedDefence")) {
            AttributeInstance shield = player.getAttribute(ModAttributes.JUSTIFIED_DEFENCE.get());
            if (shield != null) {
                shield.setBaseValue(tag.getDouble("tlsJustifiedDefence"));
            }
        }
        if (tag.contains("tlsMaxJustifiedDefence")) {
            AttributeInstance maxShield = player.getAttribute(ModAttributes.MAX_JUSTIFIED_DEFENCE.get());
            if (maxShield != null) {
                maxShield.setBaseValue(tag.getDouble("tlsMaxJustifiedDefence"));
            }
        }
    }
}
