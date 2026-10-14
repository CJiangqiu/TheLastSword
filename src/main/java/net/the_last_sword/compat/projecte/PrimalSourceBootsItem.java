package net.the_last_sword.compat.projecte;

import com.google.common.collect.Multimap;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.the_last_sword.compat.projecte.client.PrimalSourceArmorClientExtension;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.List;

public final class PrimalSourceBootsItem extends GemFeet implements PrimalSourceArmorItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PrimalSourceBootsItem() {
        super(PrimalSourceArmorSupport.properties());
    }

    @Override
    public EquipmentSlot getPrimalArmorSlot() {
        return EquipmentSlot.FEET;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return PrimalSourceArmorSupport.addAttributes(super.getAttributeModifiers(slot, stack), slot, this);
    }

    @Override
    public int getEnchantmentValue() {
        return 64;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, level, tooltips, flags);
        tooltips.removeIf(line -> line.getContents() instanceof TranslatableContents text
            && text.getKey().equals(PELang.GEM_LORE_FEET.getTranslationKey()));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new PrimalSourceArmorClientExtension<>());
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "the_last_sword:textures/item/primal_source_armor_1.png";
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
