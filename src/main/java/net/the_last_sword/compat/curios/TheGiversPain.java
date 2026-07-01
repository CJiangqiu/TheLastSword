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

//给予者的痛苦 - hands槽位
public class TheGiversPain extends Item implements ICurioItem {

    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("b2c3d4e5-f6a7-4b9c-8d1e-2f3a4b5c6d7e");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("c3d4e5f6-a7b8-4c0d-9e2f-3a4b5c6d7e8f");

    public TheGiversPain() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //攻击力+100%
        modifiers.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(ATTACK_DAMAGE_UUID, "the_givers_pain_attack_damage",
                TheLastSwordConfiguration.getCuriosGiversPainAttackDamageBonusSafely(),
                AttributeModifier.Operation.MULTIPLY_TOTAL));

        //攻击速度+100%
        modifiers.put(Attributes.ATTACK_SPEED,
            new AttributeModifier(ATTACK_SPEED_UUID, "the_givers_pain_attack_speed",
                TheLastSwordConfiguration.getCuriosGiversPainAttackSpeedBonusSafely(),
                AttributeModifier.Operation.MULTIPLY_TOTAL));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String effectSeconds = String.format("%.1f",
            TheLastSwordConfiguration.getCuriosGiversPainEffectDurationSafely() / 20.0);
        String attackMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainAttackDamageMultiplierSafely());
        String attackerMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainAttackerLostHealthMultiplierSafely());
        String targetMult = String.format("%.2f",
            TheLastSwordConfiguration.getCuriosGiversPainTargetLostHealthMultiplierSafely());
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.the_givers_pain",
            effectSeconds, attackMult, attackerMult, targetMult));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.the_givers_pain")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
