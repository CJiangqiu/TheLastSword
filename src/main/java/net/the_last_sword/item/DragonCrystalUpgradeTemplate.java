package net.the_last_sword.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class DragonCrystalUpgradeTemplate extends SmithingTemplateItem implements IDragonSmithingTemplate {

    private static final Component DRAGON_CRYSTAL_UPGRADE =
        Component.translatable("item.the_last_sword.dragon_crystal_upgrade_template").withStyle(ChatFormatting.GRAY);
    private static final Component DRAGON_CRYSTAL_UPGRADE_APPLIES_TO =
        Component.translatable("item.the_last_sword.dragon_crystal_upgrade_template.applies_to").withStyle(ChatFormatting.BLUE);
    private static final Component DRAGON_CRYSTAL_UPGRADE_INGREDIENTS =
        Component.translatable("item.the_last_sword.dragon_crystal_upgrade_template.ingredients").withStyle(ChatFormatting.BLUE);
    private static final Component DRAGON_CRYSTAL_UPGRADE_BASE_SLOT_DESCRIPTION =
        Component.translatable("item.the_last_sword.dragon_crystal_upgrade_template.base_slot_description");
    private static final Component DRAGON_CRYSTAL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION =
        Component.translatable("item.the_last_sword.dragon_crystal_upgrade_template.additions_slot_description");

    private static final ResourceLocation EMPTY_SLOT_SWORD = new ResourceLocation("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");

    public DragonCrystalUpgradeTemplate() {
        super(
            DRAGON_CRYSTAL_UPGRADE_APPLIES_TO,
            DRAGON_CRYSTAL_UPGRADE_INGREDIENTS,
            DRAGON_CRYSTAL_UPGRADE,
            DRAGON_CRYSTAL_UPGRADE_BASE_SLOT_DESCRIPTION,
            DRAGON_CRYSTAL_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
            createDragonCrystalUpgradeIconList(),
            createDragonCrystalUpgradeMaterialList()
        );
    }

    //创建基础槽空图标列表
    private static List<ResourceLocation> createDragonCrystalUpgradeIconList() {
        return List.of(
            EMPTY_SLOT_SWORD,
            EMPTY_SLOT_HELMET,
            EMPTY_SLOT_CHESTPLATE,
            EMPTY_SLOT_LEGGINGS,
            EMPTY_SLOT_BOOTS
        );
    }

    //创建附加槽空图标列表
    private static List<ResourceLocation> createDragonCrystalUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_INGOT);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_upgrade_template").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public boolean isFireResistant() {
        return true;
    }
}
