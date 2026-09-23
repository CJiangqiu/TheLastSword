package net.the_last_sword.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
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
import net.the_last_sword.init.ModAttributes;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//龙水晶项链 - necklace槽位
public class DragonCrystalNecklace extends Item implements ICurioItem {

    private static final UUID NECKLACE_UUID = UUID.fromString("a1b2c3d4-e5f6-4a5b-8c7d-9e0f1a2b3c4d");
    private static final UUID RECOVERY_SPEED_UUID = UUID.fromString("d9c8cfe2-311f-4e13-9497-d7452104dc5e");

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

        //肃正防御恢复速度 +50%
        modifiers.put(ModAttributes.JUSTIFIED_DEFENCE_RECOVERY_SPEED.get(),
            new AttributeModifier(RECOVERY_SPEED_UUID, "dragon_crystal_necklace_recovery_speed", 0.5,
                AttributeModifier.Operation.MULTIPLY_BASE));

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
            .withStyle(ChatFormatting.GRAY));
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
            immunityStr, critStr).withStyle(ChatFormatting.YELLOW));
    }
}
