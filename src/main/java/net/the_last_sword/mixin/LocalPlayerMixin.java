package net.the_last_sword.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//客户端飞行保护：对抗外部mod在客户端侧aiStep中清除LocalPlayer的flying字段
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    //玩家通过原版逻辑设定的飞行意图（外部mod的篡改不经过aiStep内的flying写入）
    @Unique
    private boolean tls$flightIntent;

    //在双击检测与移动物理之前恢复被外部清掉的flying，使本tick飞行物理正常运作
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void tls$restoreFlight(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self.isCreative() || self.isSpectator()) return;
        if (tls$flightIntent
                && self.getAbilities().mayfly
                && !self.getAbilities().flying) {
            self.getAbilities().flying = true;
        }
    }

    //跟随aiStep内对flying的每次写入更新意图（双击切换、贴地取消等均为玩家真实操作）
    @Inject(method = "aiStep", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void tls$captureIntent(CallbackInfo ci) {
        tls$flightIntent = ((LocalPlayer) (Object) this).getAbilities().flying;
    }
}
