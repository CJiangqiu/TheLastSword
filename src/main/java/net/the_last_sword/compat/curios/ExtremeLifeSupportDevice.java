package net.the_last_sword.compat.curios;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.nbt.ItemEnergyStorage;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

//极限维生装置 - belt槽位
public class ExtremeLifeSupportDevice extends Item implements ICurioItem {

    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("d4e5f6a7-b8c9-4d1e-8f3a-4b5c6d7e8f90");

    public ExtremeLifeSupportDevice() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    //Forge Energy 能力
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemEnergyStorage energyStorage = new ItemEnergyStorage(stack,
                TheLastSwordConfiguration::getCuriosExtremeLifeSupportMaxEnergySafely);
            private final LazyOptional<ItemEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                if (cap == ForgeCapabilities.ENERGY) {
                    return energyCap.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> {
                int maxEnergy = energy.getMaxEnergyStored();
                if (maxEnergy == 0) return 0;
                return Math.round(13.0F * energy.getEnergyStored() / maxEnergy);
            })
            .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(energy -> {
                int maxEnergy = energy.getMaxEnergyStored();
                if (maxEnergy == 0) return 0x8B00FF;
                float ratio = (float) energy.getEnergyStored() / maxEnergy;
                if (ratio < 0.25F) {
                    return 0xFF0000;
                } else if (ratio < 0.5F) {
                    return 0xFF8C00;
                } else if (ratio < 0.75F) {
                    return 0x9B30FF;
                } else {
                    return 0xBF00FF;
                }
            })
            .orElse(0x8B00FF);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        //盔甲韧性+100
        modifiers.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(ARMOR_TOUGHNESS_UUID, "extreme_life_support_armor_toughness",
                TheLastSwordConfiguration.getCuriosExtremeLifeSupportArmorToughnessSafely(),
                AttributeModifier.Operation.ADDITION));

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        //能量信息
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy ->
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.energy")
                .append(": §a" + energy.getEnergyStored() + " §r/ " + energy.getMaxEnergyStored() + " FE")));
        int energyCost = TheLastSwordConfiguration.getCuriosExtremeLifeSupportEnergyCostSafely();
        String thresholdPercent = String.format("%.0f",
            TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely() * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.extreme_life_support_device", energyCost, thresholdPercent));
        appendCurrentValueLine(tooltip);
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.extreme_life_support_device")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    //基于客户端玩家当前生命比例显示损伤百分比与额外Buff等级
    @OnlyIn(Dist.CLIENT)
    private static void appendCurrentValueLine(List<Component> tooltip) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0) {
            return;
        }
        double lostRatio = 1.0 - (player.getHealth() / maxHealth);
        if (lostRatio < 0) {
            lostRatio = 0;
        }
        double threshold = TheLastSwordConfiguration.getCuriosExtremeLifeSupportTierThresholdSafely();
        int bonusLevel = threshold > 0 ? (int) (lostRatio / threshold) : 0;
        String lostStr = String.format("%.1f", lostRatio * 100);
        tooltip.add(Component.translatable("item_tooltip.the_last_sword.extreme_life_support_device.current",
            lostStr, bonusLevel).withStyle(net.minecraft.ChatFormatting.YELLOW));
    }
}
