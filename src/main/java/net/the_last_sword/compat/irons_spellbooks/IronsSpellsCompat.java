package net.the_last_sword.compat.irons_spellbooks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.item.DragonArmorItem;
import net.the_last_sword.item.DragonCultPriestArmorItem;
import net.the_last_sword.item.DragonCrystalArmorItem;
import net.the_last_sword.item.DragonCrystalSword;
import net.the_last_sword.item.DragonSword;
import net.the_last_sword.item.PriestStaffItem;
import net.the_last_sword.item.TheLastSword;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = TheLastSwordMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IronsSpellsCompat {

    private static final ResourceLocation MAX_MANA_ID =
        ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "max_mana");
    private static final ResourceLocation SPELL_POWER_ID =
        ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spell_power");
    //部位独立 UUID 使四件装备的同属性加成可以叠加并正确移除
    private static final UUID[] MAX_MANA_MODIFIER_UUIDS = {
        UUID.fromString("3e37a19d-7dbd-4772-a256-970c1bf30203"),
        UUID.fromString("bb985e81-a8b4-4d47-bdd5-57b167fe7638"),
        UUID.fromString("f07160a2-30f4-4348-9a7a-ce47cb0dc1e6"),
        UUID.fromString("4f50d541-7f69-479f-ace8-fb4ef17c9289")
    };
    private static final UUID[] SPELL_POWER_MODIFIER_UUIDS = {
        UUID.fromString("f9c7e10a-efeb-4ca8-bd29-0eb5e1bb37a0"),
        UUID.fromString("c61417dd-47e2-48af-a6fe-6df144b4851c"),
        UUID.fromString("09b3b2c3-a4f6-4f7f-8c39-ee57be1ee448"),
        UUID.fromString("9748dcc6-da79-4cca-a87f-e2f417947118")
    };
    private static final UUID STAFF_MAX_MANA_MODIFIER_UUID =
        UUID.fromString("940cb348-78fa-4feb-9d1b-ab82339034cd");
    private static final UUID STAFF_SPELL_POWER_MODIFIER_UUID =
        UUID.fromString("52eabe3b-ddb1-474c-bc6e-2a568ea3f872");
    private static final UUID SWORD_MAX_MANA_MODIFIER_UUID =
        UUID.fromString("0e302fe0-f794-4da7-a9c6-9bc862a71215");
    private static final UUID SWORD_SPELL_POWER_MODIFIER_UUID =
        UUID.fromString("95e86702-a5e0-4f7c-89ed-09151120c7fa");

    private IronsSpellsCompat() {
    }

    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof DragonCultPriestArmorItem armor
            && event.getSlotType() == armor.getEquipmentSlot()) {
            addArmorModifiers(event);
            return;
        }

        if (event.getItemStack().getItem() instanceof DragonCrystalArmorItem armor
            && event.getSlotType() == armor.getEquipmentSlot()) {
            addScaledArmorModifiers(event, 0.25, "Dragon crystal armor");
            return;
        }

        if (event.getItemStack().getItem() instanceof DragonArmorItem armor
            && event.getSlotType() == armor.getEquipmentSlot()) {
            addScaledArmorModifiers(event, 0.5, "Dragon armor");
            return;
        }

        if (event.getItemStack().getItem() instanceof PriestStaffItem
            && event.getSlotType() == EquipmentSlot.MAINHAND) {
            addStaffModifiers(event);
            return;
        }

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            if (event.getItemStack().getItem() instanceof DragonCrystalSword) {
                addSwordModifiers(event, 0.25, "Dragon crystal sword");
            } else if (event.getItemStack().getItem() instanceof DragonSword) {
                addSwordModifiers(event, 0.5, "Dragon sword");
            } else if (event.getItemStack().getItem() instanceof TheLastSword) {
                addSwordModifiers(event, 1.0, "The last sword");
            }
        }
    }

    private static void addArmorModifiers(ItemAttributeModifierEvent event) {
        Attribute maxMana = ForgeRegistries.ATTRIBUTES.getValue(MAX_MANA_ID);
        if (maxMana == null) {
            return;
        }

        event.addModifier(maxMana, new AttributeModifier(
            MAX_MANA_MODIFIER_UUIDS[event.getSlotType().getIndex()],
            "Dragon cult priest armor max mana",
            0.25,
            AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }

    private static void addScaledArmorModifiers(ItemAttributeModifierEvent event, double bonus, String name) {
        int slotIndex = event.getSlotType().getIndex();
        Attribute maxMana = ForgeRegistries.ATTRIBUTES.getValue(MAX_MANA_ID);
        if (maxMana != null) {
            event.addModifier(maxMana, new AttributeModifier(
                MAX_MANA_MODIFIER_UUIDS[slotIndex],
                name + " max mana",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_ID);
        if (spellPower != null) {
            event.addModifier(spellPower, new AttributeModifier(
                SPELL_POWER_MODIFIER_UUIDS[slotIndex],
                name + " spell power",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void addStaffModifiers(ItemAttributeModifierEvent event) {
        Attribute maxMana = ForgeRegistries.ATTRIBUTES.getValue(MAX_MANA_ID);
        if (maxMana != null) {
            event.addModifier(maxMana, new AttributeModifier(
                STAFF_MAX_MANA_MODIFIER_UUID,
                "Priest staff max mana",
                100.0,
                AttributeModifier.Operation.ADDITION
            ));
        }

        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_ID);
        if (spellPower != null) {
            event.addModifier(spellPower, new AttributeModifier(
                STAFF_SPELL_POWER_MODIFIER_UUID,
                "Priest staff spell power",
                0.25,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void addSwordModifiers(ItemAttributeModifierEvent event, double bonus, String name) {
        Attribute maxMana = ForgeRegistries.ATTRIBUTES.getValue(MAX_MANA_ID);
        if (maxMana != null) {
            event.addModifier(maxMana, new AttributeModifier(
                SWORD_MAX_MANA_MODIFIER_UUID,
                name + " max mana",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        Attribute spellPower = ForgeRegistries.ATTRIBUTES.getValue(SPELL_POWER_ID);
        if (spellPower != null) {
            event.addModifier(spellPower, new AttributeModifier(
                SWORD_SPELL_POWER_MODIFIER_UUID,
                name + " spell power",
                bonus,
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }
}
