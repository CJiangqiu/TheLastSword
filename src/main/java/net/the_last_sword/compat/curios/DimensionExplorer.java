package net.the_last_sword.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//维度探索者 - feet槽位
public class DimensionExplorer extends Item implements ICurioItem {

    private static final UUID SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");

    public DimensionExplorer() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //移动速度+50%
        modifiers.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(SPEED_UUID, "dimension_explorer_speed", 0.5,
                AttributeModifier.Operation.MULTIPLY_BASE));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int jumpLevel = TheLastSwordConfiguration.getCuriosDimensionExplorerJumpAmplifierSafely() + 1;
        int hasteLevel = TheLastSwordConfiguration.getCuriosDimensionExplorerHasteAmplifierSafely() + 1;
        int speedLevel = TheLastSwordConfiguration.getCuriosDimensionExplorerSpeedAmplifierSafely() + 1;
        String emergencyHealth = String.format("%.1f",
            TheLastSwordConfiguration.getCuriosDimensionExplorerEmergencyHealHealthSafely());
        int emergencyFood = TheLastSwordConfiguration.getCuriosDimensionExplorerEmergencyFoodLevelSafely();
        String effectSeconds = String.format("%.1f",
            TheLastSwordConfiguration.getCuriosDimensionExplorerEffectDurationSafely() / 20.0);
        String cooldownSeconds = String.format("%.1f",
            TheLastSwordConfiguration.getCuriosDimensionExplorerCooldownSafely() / 20.0);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.dimension_explorer",
            jumpLevel, emergencyHealth, emergencyFood, hasteLevel, speedLevel, effectSeconds, cooldownSeconds));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dimension_explorer")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
