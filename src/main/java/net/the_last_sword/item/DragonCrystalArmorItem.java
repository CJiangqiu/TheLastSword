package net.the_last_sword.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 龙水晶盔甲
 * 基于NBT系统，使用ItemLevelHelper和ItemJustifiedDefenceHelper
 */
public abstract class DragonCrystalArmorItem extends TheLastEndArmorItem {

    public DragonCrystalArmorItem(Type type, Properties props) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(Type t) {
                return new int[]{407*2, 592*2, 555*2, 481*2}[t.getSlot().getIndex()];
            }

            @Override
            public int getDefenseForType(Type t) {
                return new int[]{4, 6, 6, 4}[t.getSlot().getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return 22;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(new ItemStack(ModItems.DRAGON_CRYSTAL.get()));
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
        }, type, props.fireResistant().rarity(Rarity.RARE));
    }

    // ==================== 实现抽象方法 ====================

    @Override
    protected int[] getBaseArmorValues() {
        return new int[]{4, 6, 6, 4}; // [靴子, 护腿, 胸甲, 头盔]
    }

    @Override
    protected double getBaseToughness() {
        return 5.0;
    }

    @Override
    protected String getArmorName() {
        return "DragonCrystalArmor";
    }

    @Override
    protected void appendSpecificTooltip(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable(getSpecificTooltipKey()));

        //水晶守护技能说明（显示刷新时间）
        int refreshInterval = TheLastSwordConfiguration.getCrystalGuardRefreshIntervalSafely();
        int seconds = refreshInterval / 20;
        list.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_armor_skill", seconds));

        list.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_crystal_armor")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    protected abstract String getSpecificTooltipKey();

    // ==================== 全套检查方法 ====================

    //检查实体是否穿戴全套龙水晶盔甲
    public static boolean isFullSet(LivingEntity entity) {
        return !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
               entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DragonCrystalArmorItem.Helmet &&
               !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
               entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DragonCrystalArmorItem.Chestplate &&
               !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
               entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DragonCrystalArmorItem.Leggings &&
               !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty() &&
               entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof DragonCrystalArmorItem.Boots;
    }

    // ==================== 4个子类：头盔、胸甲、护腿、靴子 ====================

    public static class Helmet extends DragonCrystalArmorItem {
        public Helmet() {
            super(Type.HELMET, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack s, Entity e, EquipmentSlot sl, String tp) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.HEAD;
        }

        @Override
        protected String getSpecificTooltipKey() {
            return "item_tooltip.the_last_sword.dragon_crystal_armor_helmet";
        }
    }

    public static class Chestplate extends DragonCrystalArmorItem {
        public Chestplate() {
            super(Type.CHESTPLATE, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack s, Entity e, EquipmentSlot sl, String tp) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.CHEST;
        }

        @Override
        protected String getSpecificTooltipKey() {
            return "item_tooltip.the_last_sword.dragon_crystal_armor_chestplate";
        }
    }

    public static class Leggings extends DragonCrystalArmorItem {
        public Leggings() {
            super(Type.LEGGINGS, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack s, Entity e, EquipmentSlot sl, String tp) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_2.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.LEGS;
        }

        @Override
        protected String getSpecificTooltipKey() {
            return "item_tooltip.the_last_sword.dragon_crystal_armor_leggings";
        }
    }

    public static class Boots extends DragonCrystalArmorItem {
        public Boots() {
            super(Type.BOOTS, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack s, Entity e, EquipmentSlot sl, String tp) {
            return "the_last_sword:textures/models/armor/dragon_crystal_armor_layer_1.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.FEET;
        }

        @Override
        protected String getSpecificTooltipKey() {
            return "item_tooltip.the_last_sword.dragon_crystal_armor_boots";
        }
    }
}
