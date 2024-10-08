package net.thelastsword.item;

import com.google.common.collect.Iterables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.thelastsword.init.TheLastSwordModItems;

public abstract class DragonCrystalArmor extends ArmorItem {
    public DragonCrystalArmor(ArmorItem.Type type, Item.Properties properties) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return new int[]{26, 30, 32, 22}[type.getSlot().getIndex()] * 74;
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return new int[]{4, 6, 6, 4}[type.getSlot().getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return 22;
            }

            @Override
            public SoundEvent getEquipSound() {
                return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.armor.equip_netherite"));
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(TheLastSwordModItems.DRAGON_CRYSTAL.get()));
            }

            @Override
            public String getName() {
                return "dragon_crystal_armor";
            }

            @Override
            public float getToughness() {
                return 5f;
            }

            @Override
            public float getKnockbackResistance() {
                return 1f;
            }
        }, type, properties.fireResistant().rarity(Rarity.RARE));
    }



    public static class Helmet extends DragonCrystalArmor {
        public Helmet() {
            super(ArmorItem.Type.HELMET, new Item.Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 4, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 240, 4, false, false));
            }
        }
    }

    public static class Chestplate extends DragonCrystalArmor {
        public Chestplate() {
            super(ArmorItem.Type.CHESTPLATE, new Item.Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 3, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 4, false, false));

            }
        }
    }

    public static class Leggings extends DragonCrystalArmor {
        public Leggings() {
            super(ArmorItem.Type.LEGGINGS, new Item.Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_2.png";
        }

        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 4, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP, 240, 2, false, false));
            }
        }
    }

    public static class Boots extends DragonCrystalArmor {
        public Boots() {
            super(ArmorItem.Type.BOOTS, new Item.Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
            super.inventoryTick(itemstack, world, entity, slot, selected);
            if (entity instanceof LivingEntity livingEntity && Iterables.contains(livingEntity.getArmorSlots(), itemstack)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 2, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240, 4, false, false));
            }
        }
    }
}
