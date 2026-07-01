package net.the_last_sword.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//龙水晶项链 - necklace槽位
public class DragonCrystalNecklace extends Item implements ICurioItem {

    private static final UUID NECKLACE_UUID = UUID.fromString("a1b2c3d4-e5f6-4a5b-8c7d-9e0f1a2b3c4d");

    public DragonCrystalNecklace() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //最大生命值 -10
        modifiers.put(Attributes.MAX_HEALTH,
            new AttributeModifier(NECKLACE_UUID, "dragon_crystal_necklace_health", -10.0,
                AttributeModifier.Operation.ADDITION));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String immuneBase = String.format("%.0f", TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceBaseSafely() * 100);
        String immunePerLuck = String.format("%.0f", TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChancePerLuckSafely() * 100);
        String immuneMax = String.format("%.0f", TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceMaxSafely() * 100);
        String critBase = String.format("%.0f", TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChanceBaseSafely() * 100);
        String critPerLuck = String.format("%.0f", TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChancePerLuckSafely() * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_necklace",
            immuneBase, immunePerLuck, immuneMax, critBase, critPerLuck));
        appendCurrentValueLine(tooltip);
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_necklace")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    //基于客户端玩家幸运值显示实时免疫概率与暴击加成
    @OnlyIn(Dist.CLIENT)
    private static void appendCurrentValueLine(List<Component> tooltip) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        double luck = player.getAttributeValue(Attributes.LUCK);
        double immunity = Math.min(
            TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceMaxSafely(),
            TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChanceBaseSafely()
                + luck * TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceImmunityChancePerLuckSafely()
        );
        double crit = TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChanceBaseSafely()
            + luck * TheLastSwordConfiguration.getCuriosDragonCrystalNecklaceCritChancePerLuckSafely();
        String immunityStr = String.format("%.1f", immunity * 100);
        String critStr = String.format("%.1f", crit * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_necklace.current",
            immunityStr, critStr).withStyle(net.minecraft.ChatFormatting.YELLOW));
    }
}
