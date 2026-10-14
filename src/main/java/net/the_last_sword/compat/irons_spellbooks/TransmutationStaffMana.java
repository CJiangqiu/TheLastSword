package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

public final class TransmutationStaffMana {
    private TransmutationStaffMana() {
    }

    public static float drain(ServerPlayer player) {
        MagicData data = MagicData.getPlayerMagicData(player);
        float before = data.getMana();
        if (!Float.isFinite(before) || before <= 0.0F) {
            return 0.0F;
        }
        data.setMana(0.0F);
        PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
        // 以实际扣除量计算加伤，避免余额变更被拦截后仍获得伤害。
        float after = data.getMana();
        return Float.isFinite(after) ? Math.max(0.0F, before - Math.max(0.0F, after)) : 0.0F;
    }
}
