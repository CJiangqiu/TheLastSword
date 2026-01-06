package net.the_last_sword.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//踢出和危险数据包防护
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "disconnect",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockKick(Component reason, CallbackInfo ci) {
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            if (EntityUtil.hasProtection(this.player)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockDangerPackets(Packet<?> packet, CallbackInfo ci) {
        if (!TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            return;
        }

        boolean hasProtection = EntityUtil.hasProtection(this.player);

        if (packet instanceof ClientboundDisconnectPacket) {
            if (hasProtection) {
                ci.cancel();
            }
            return;
        }
        if (packet instanceof ClientboundSetHealthPacket) {
            if (hasProtection) {
                ci.cancel();
            }
        }
    }
}
