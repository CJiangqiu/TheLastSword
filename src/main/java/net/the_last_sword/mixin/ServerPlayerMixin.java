package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//玩家重生时恢复受保护的背包
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    //死亡重生后，若旧玩家有背包保护则强制恢复背包（原版 keepInventory=false 时不复制）
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void tls$restoreProtectedInventory(ServerPlayer oldPlayer, boolean keepAllInventory, CallbackInfo ci) {
        if (!keepAllInventory
                && !((Player) (Object) this).level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                && !oldPlayer.isSpectator()
                && EntityUtil.hasInventoryProtection(oldPlayer)) {
            ((Player) (Object) this).getInventory().replaceWith(oldPlayer.getInventory());
        }
    }
}
