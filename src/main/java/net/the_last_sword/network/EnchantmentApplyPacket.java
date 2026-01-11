package net.the_last_sword.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.block.entity.DragonCrystalEnchantingTableBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// 附魔应用网络包
public class EnchantmentApplyPacket {

    private final BlockPos pos;
    private final Map<ResourceLocation, Integer> enchantments;

    public EnchantmentApplyPacket(BlockPos pos, Map<ResourceLocation, Integer> enchantments) {
        this.pos = pos;
        this.enchantments = enchantments;
    }

    public static EnchantmentApplyPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readInt();
        Map<ResourceLocation, Integer> enchantments = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int level = buf.readInt();
            enchantments.put(id, level);
        }
        return new EnchantmentApplyPacket(pos, enchantments);
    }

    public static void encode(EnchantmentApplyPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.enchantments.size());
        for (var entry : msg.enchantments.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public static void handle(EnchantmentApplyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (!(be instanceof DragonCrystalEnchantingTableBlockEntity enchantingTable)) return;

            // 验证玩家距离
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64) return;

            ItemStack stack = enchantingTable.getItem(1);
            if (stack.isEmpty()) return;

            // 转换附魔ID为Enchantment对象，同时计算累加后的等级
            Map<Enchantment, Integer> enchantmentAddLevels = new HashMap<>();
            for (var entry : msg.enchantments.entrySet()) {
                Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(entry.getKey());
                if (ench != null) {
                    enchantmentAddLevels.put(ench, entry.getValue());
                }
            }

            if (enchantmentAddLevels.isEmpty()) return;

            // 计算总消耗：10240 * 增加的等级
            long totalCost = 0;
            for (int addLevel : enchantmentAddLevels.values()) {
                totalCost += 10240L * addLevel;
            }

            // 检查电量
            long currentEnergy = enchantingTable.getEnergyStorage().getEnergyStored();
            if (totalCost > currentEnergy) return;

            // 消耗电量
            int toExtract = (int) Math.min(totalCost, Integer.MAX_VALUE);
            enchantingTable.extractEnergy(toExtract);

            // 应用附魔（累加模式，上限255）
            Map<Enchantment, Integer> existingEnchants = EnchantmentHelper.getEnchantments(stack);
            for (var entry : enchantmentAddLevels.entrySet()) {
                int currentLevel = existingEnchants.getOrDefault(entry.getKey(), 0);
                int newLevel = Math.min(255, currentLevel + entry.getValue());
                existingEnchants.put(entry.getKey(), newLevel);
            }
            EnchantmentHelper.setEnchantments(existingEnchants, stack);

            enchantingTable.setChanged();
        });
        ctx.get().setPacketHandled(true);
    }
}
