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

//龙水晶王冠 - head槽位
public class DragonCrystalCrown extends Item implements ICurioItem {

    private static final UUID CROWN_UUID = UUID.fromString("b2c3d4e5-f6a7-5b6c-9d8e-0f1a2b3c4d5e");

    public DragonCrystalCrown() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //最大生命值 +200
        modifiers.put(Attributes.MAX_HEALTH,
            new AttributeModifier(CROWN_UUID, "dragon_crystal_crown_health", 200.0,
                AttributeModifier.Operation.ADDITION));

        //攻击力 +20
        modifiers.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(UUID.fromString("b2c3d4e5-f6a7-5b6c-9d8e-0f1a2b3c4d5f"),
                "dragon_crystal_crown_attack", 20.0,
                AttributeModifier.Operation.ADDITION));

        //盔甲值 +20
        modifiers.put(Attributes.ARMOR,
            new AttributeModifier(UUID.fromString("b2c3d4e5-f6a7-5b6c-9d8e-0f1a2b3c4d60"),
                "dragon_crystal_crown_armor", 20.0,
                AttributeModifier.Operation.ADDITION));

        //盔甲韧性 +20
        modifiers.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(UUID.fromString("b2c3d4e5-f6a7-5b6c-9d8e-0f1a2b3c4d61"),
                "dragon_crystal_crown_toughness", 20.0,
                AttributeModifier.Operation.ADDITION));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        //根据虚空转换开关显示不同提示
        String key = TheLastSwordConfiguration.getCuriosDragonCrystalCrownVoidConversionEnabledSafely()
            ? "item_tooltip.the_last_sword.dragon_crystal_crown.enabled"
            : "item_tooltip.the_last_sword.dragon_crystal_crown.disabled";
        tooltip.add(Component.translatable(key));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_crown")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
