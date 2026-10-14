package net.the_last_sword.compat.projecte;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.the_last_sword.init.ModAttributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PrimalSourceArmorSupport {
    private PrimalSourceArmorSupport() {
    }

    public static Item create(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> new PrimalSourceHelmetItem();
            case CHESTPLATE -> new PrimalSourceChestplateItem();
            case LEGGINGS -> new PrimalSourceLeggingsItem();
            case BOOTS -> new PrimalSourceBootsItem();
        };
    }

    public static Item.Properties properties() {
        return new Item.Properties().fireResistant().rarity(Rarity.EPIC);
    }

    public static Multimap<Attribute, AttributeModifier> addAttributes(
            Multimap<Attribute, AttributeModifier> base, EquipmentSlot slot, PrimalSourceArmorItem armor) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create(base);
        if (slot == armor.getPrimalArmorSlot()) {
            // 沿用各部位的固定标识，确保替换装备后属性正确移除。
            UUID maxDefenceId = UUID.nameUUIDFromBytes(
                ("the_last_sword:primal_source_max_defence_" + slot.getName()).getBytes(StandardCharsets.UTF_8));
            UUID recoveryId = UUID.nameUUIDFromBytes(
                ("the_last_sword:primal_source_defence_recovery_" + slot.getName()).getBytes(StandardCharsets.UTF_8));
            modifiers.put(ModAttributes.MAX_JUSTIFIED_DEFENCE.get(), new AttributeModifier(
                maxDefenceId, "Primal source armor max justified defence", 2.0, AttributeModifier.Operation.ADDITION));
            modifiers.put(ModAttributes.JUSTIFIED_DEFENCE_RECOVERY_SPEED.get(), new AttributeModifier(
                recoveryId, "Primal source armor justified defence recovery", 0.5, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        return modifiers;
    }

    public static void tickFluidWalking(Player player, Level level, TagKey<Fluid> fluid) {
        BlockPos pos = new BlockPos(
            (int) Math.floor(player.getX()),
            (int) (player.getY() - player.getMyRidingOffset()),
            (int) Math.floor(player.getZ()));
        // 液面行走的原入口限定了物品身份，需为新装备补齐相同行为。
        if (level.getFluidState(pos.below()).is(fluid) && level.isEmptyBlock(pos)) {
            if (!player.isShiftKeyDown()) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
                player.fallDistance = 0;
                player.setOnGround(true);
            }
        } else if (!level.isClientSide && fluid.equals(FluidTags.WATER) && player.isInWater()) {
            player.setAirSupply(player.getMaxAirSupply());
        }
    }
}
