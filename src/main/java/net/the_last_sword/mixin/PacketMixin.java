package net.the_last_sword.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

public final class PacketMixin {
    private PacketMixin() {}

    /* ─────────── 1. 清包保护 ─────────── */
    @Mixin(Inventory.class)
    public static abstract class ClearPrevent {

        @Final
        @Shadow public Player player;

        @Inject(method = "clearOrCountMatchingItems",
                at = @At("HEAD"), cancellable = true)
        private void tls$blockClear(Predicate<ItemStack> predicate,
                                    int maxCount,
                                    Container container,
                                    CallbackInfoReturnable<Integer> cir) {
            if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
                int level = DefenceManager.hasDefenceRecord(this.player) ? DefenceManager.getDefenceLevel(this.player) : 0;
                if (level >= 2) {
                    cir.setReturnValue(0);
                }
            }
        }

        @Inject(method = "clearContent",
                at = @At("HEAD"), cancellable = true)
        private void tls$blockClearContent(CallbackInfo ci) {
            if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
                int level = DefenceManager.hasDefenceRecord(this.player) ? DefenceManager.getDefenceLevel(this.player) : 0;
                if (level >= 2) {
                    ci.cancel();
                }
            }
        }
    }

    /* ─────────── 2. 踢人 & 断线包 保护 ─────────── */
    @Mixin(ServerGamePacketListenerImpl.class)
    public static abstract class KickPrevent {

        @Shadow public ServerPlayer player;

        @Inject(method = "disconnect",
                at = @At("HEAD"), cancellable = true)
        private void tls$blockKick(Component reason, CallbackInfo ci) {
            if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
                int level = DefenceManager.hasDefenceRecord(this.player) ? DefenceManager.getDefenceLevel(this.player) : 0;
                if (level >= 2) {
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

            int playerLevel = DefenceManager.hasDefenceRecord(this.player) ? DefenceManager.getDefenceLevel(this.player) : 0;

            if (packet instanceof ClientboundDisconnectPacket) {
                if (playerLevel >= 2) {
                    ci.cancel();
                }
                return;
            }
            if (packet instanceof ClientboundSetHealthPacket) {
                if (playerLevel >= 2) {
                    ci.cancel();
                }
                return;
            }

            // 实体相关包 - 直接检查目标实体的防御等级
            if (packet instanceof ClientboundRemoveEntitiesPacket removePacket) {
                if (theLastSword$containsProtectedEntity(removePacket)) {
                    ci.cancel();   // 不发送实体移除包
                }
                return;
            }
            if (packet instanceof ClientboundTeleportEntityPacket teleportPacket) {
                if (theLastSword$isProtectedEntityTeleport(teleportPacket)) {
                    ci.cancel();   // 不发送传送包
                }
                return;
            }
            if (packet instanceof ClientboundEntityEventPacket eventPacket) {
                if (theLastSword$isProtectedEntityEvent(eventPacket)) {
                    ci.cancel();   // 不发送事件包
                }
                return;
            }
        }

        @Unique
        private boolean theLastSword$containsProtectedEntity(ClientboundRemoveEntitiesPacket packet) {
            IntList entityIds = packet.getEntityIds();

            for (int entityId : entityIds) {
                Entity entity = this.player.level().getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity) {
                    int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
                    if (level >= 2) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Unique
        private boolean theLastSword$isProtectedEntityTeleport(ClientboundTeleportEntityPacket packet) {
            int entityId = packet.getId();

            Entity entity = this.player.level().getEntity(entityId);
            if (entity instanceof LivingEntity livingEntity) {
                int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
                return level >= 2;
            }
            return false;
        }

        @Unique
        private boolean theLastSword$isProtectedEntityEvent(ClientboundEntityEventPacket packet) {
            byte eventId = packet.getEventId();

            Entity entity = packet.getEntity(this.player.level());
            if (entity instanceof LivingEntity livingEntity) {
                int level = DefenceManager.hasDefenceRecord(livingEntity) ? DefenceManager.getDefenceLevel(livingEntity) : 0;
                if (level >= 2) {
                    return eventId == 3 || eventId == 60;
                }
            }
            return false;
        }
    }

    /* ─────────── 3. Connection 兜底 ─────────── */
    @Mixin(Connection.class)
    public static abstract class ConnPrevent {

        @Shadow private net.minecraft.network.PacketListener packetListener;

        @Inject(method = "disconnect",
                at = @At("HEAD"), cancellable = true)
        private void tls$blockConnDisconnect(Component reason, CallbackInfo ci) {
            if (this.packetListener instanceof ServerGamePacketListenerImpl listener) {
                ServerPlayer sp = listener.player;
                if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely() && sp != null) {
                    int level = DefenceManager.hasDefenceRecord(sp) ? DefenceManager.getDefenceLevel(sp) : 0;
                    if (level >= 2) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
