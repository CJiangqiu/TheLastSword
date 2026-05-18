package net.the_last_sword.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
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
}
