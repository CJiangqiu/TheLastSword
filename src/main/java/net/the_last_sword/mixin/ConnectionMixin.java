package net.the_last_sword.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//连接断开兜底防护
@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow private PacketListener packetListener;

    @Inject(method = "disconnect",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockConnDisconnect(Component reason, CallbackInfo ci) {
        if (this.packetListener instanceof ServerGamePacketListenerImpl listener) {
            ServerPlayer sp = listener.player;
            if (sp != null && EntityUtil.hasProtection(sp)) {
                ci.cancel();
            }
        }
    }
}
