package net.the_last_sword.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//玩家相关注入
@Mixin(Player.class)
public class PlayerMixin {

    //防止外部代码替换受保护玩家的装备槽
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void tls$blockSetItemSlot(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (EntityUtil.hasProtection((Player) (Object) this)) {
            ci.cancel();
        }
    }
}
