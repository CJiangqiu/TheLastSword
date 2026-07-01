package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.renderer.DragonShieldRenderer;

import java.util.function.Supplier;

public class DragonShieldPacket {

    private final boolean hasDirection;
    private final float directionX;
    private final float directionY;
    private final float directionZ;

    public DragonShieldPacket(boolean hasDirection, float directionX, float directionY, float directionZ) {
        this.hasDirection = hasDirection;
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
    }

    public static DragonShieldPacket fromDamageSource(ServerPlayer player, DamageSource source) {
        Vec3 sourcePos = source.getSourcePosition();
        Entity sourceEntity = source.getEntity();
        if (sourcePos == null && sourceEntity != null) {
            sourcePos = sourceEntity.position().add(0.0, sourceEntity.getBbHeight() * 0.5, 0.0);
        }
        if (sourcePos == null) {
            return new DragonShieldPacket(false, 0.0f, 0.0f, 0.0f);
        }

        Vec3 playerPos = player.position().add(0.0, player.getBbHeight() * 0.5, 0.0);
        Vec3 direction = sourcePos.subtract(playerPos);
        if (direction.lengthSqr() < 0.0001) {
            return new DragonShieldPacket(false, 0.0f, 0.0f, 0.0f);
        }

        direction = direction.normalize();
        return new DragonShieldPacket(true, (float) direction.x, (float) direction.y, (float) direction.z);
    }

    public static void encode(DragonShieldPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.hasDirection);
        if (msg.hasDirection) {
            buf.writeFloat(msg.directionX);
            buf.writeFloat(msg.directionY);
            buf.writeFloat(msg.directionZ);
        }
    }

    public static DragonShieldPacket decode(FriendlyByteBuf buf) {
        boolean hasDirection = buf.readBoolean();
        if (!hasDirection) {
            return new DragonShieldPacket(false, 0.0f, 0.0f, 0.0f);
        }
        return new DragonShieldPacket(true, buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(DragonShieldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.hasDirection) {
                DragonShieldRenderer.trigger(msg.directionX, msg.directionY, msg.directionZ);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
