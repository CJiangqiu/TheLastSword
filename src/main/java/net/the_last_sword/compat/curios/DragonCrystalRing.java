package net.the_last_sword.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.the_last_sword.init.ModAttributes;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//龙水晶指环 - ring槽位
public class DragonCrystalRing extends Item implements ICurioItem {

    private static final UUID RING_UUID = UUID.fromString("c3d4e5f6-a7b8-6c7d-0e1f-2a3b4c5d6e7f");

    public DragonCrystalRing() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //最大肃正防御 +6
        modifiers.put(ModAttributes.MAX_JUSTIFIED_DEFENCE.get(),
            new AttributeModifier(RING_UUID, "dragon_crystal_ring_defence", 6.0,
                AttributeModifier.Operation.ADDITION));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_ring"));
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_ring")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
