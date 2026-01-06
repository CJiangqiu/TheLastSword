package net.the_last_sword.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.the_last_sword.client.renderer.DragonArmorRenderer;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.util.nbt.ItemEnergyStorage;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import net.minecraft.nbt.CompoundTag;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * 龙之盔甲
 * 基于NBT等级系统 + Forge Energy + GeckoLib动画
 */
public abstract class DragonArmorItem extends TheLastEndArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public String animationprocedure = "empty";

    //能量基础值（6级龙套的基础能量）
    private static final int BASE_ENERGY = 1048576; // 1M FE

    public DragonArmorItem(Type type, Properties props) {
        super(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(Type t) {
                return Integer.MAX_VALUE; // 2,147,483,647 耐久度
            }

            @Override
            public int getDefenseForType(Type t) {
                return new int[]{8, 12, 12, 8}[t.getSlot().getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return 200;
            }

            @Override
            public SoundEvent getEquipSound() {
                return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.beacon.activate"));
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of();
            }

            @Override
            public String getName() {
                return "dragon_armor";
            }

            @Override
            public float getToughness() {
                return 10f;
            }

            @Override
            public float getKnockbackResistance() {
                return 5f;
            }
        }, type, props.fireResistant().rarity(Rarity.EPIC));
    }
    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        //龙之套装不掉耐久
        return 0;
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                          EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = new DragonArmorRenderer();
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    // ==================== 实现TheLastEndArmorItem抽象方法 ====================

    //龙套装初始等级6级
    @Override
    protected int getDefaultLevel() {
        return 6;
    }

    @Override
    protected int[] getBaseArmorValues() {
        return new int[]{8, 12, 12, 8}; // [靴子, 护腿, 胸甲, 头盔]
    }

    @Override
    protected double getBaseToughness() {
        return 10.0;
    }

    @Override
    protected String getArmorName() {
        return "DragonArmor";
    }

    // ==================== Forge Energy系统 ====================

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemEnergyStorage energyStorage = new ItemEnergyStorage(stack,
                () -> DragonArmorItem.getMaxEnergy(stack));
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
        return true; // 始终显示能量条
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> {
                    int maxEnergy = energy.getMaxEnergyStored();
                    int currentEnergy = energy.getEnergyStored();
                    if (maxEnergy == 0) return 0;
                    return Math.round(13.0F * currentEnergy / maxEnergy);
                })
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> {
                    int maxEnergy = energy.getMaxEnergyStored();
                    int currentEnergy = energy.getEnergyStored();
                    if (maxEnergy == 0) return 0x8B00FF;

                    float ratio = (float) currentEnergy / maxEnergy;

                    if (ratio < 0.25F) {
                        return 0xFF0000; // 红色
                    } else if (ratio < 0.5F) {
                        return 0xFF8C00; // 橙色
                    } else if (ratio < 0.75F) {
                        return 0x9B30FF; // 紫色
                    } else {
                        return 0xBF00FF; // 亮紫色
                    }
                })
                .orElse(0x8B00FF);
    }

    @Override
    protected void appendSpecificTooltip(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        // 显示能量信息
        itemstack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            list.add(Component.translatable("item_tooltip.the_last_sword.energy")
                    .append(": §a" + energy.getEnergyStored() + " §r/ " + energy.getMaxEnergyStored() + " FE"));
        });

        // 部件特定描述
        EquipmentSlot slot = this.getEquipmentSlot();
        switch (slot) {
            case HEAD -> list.add(Component.translatable("item_tooltip.the_last_sword.dragon_armor_helmet"));
            case CHEST -> list.add(Component.translatable("item_tooltip.the_last_sword.dragon_armor_chestplate"));
            case LEGS -> list.add(Component.translatable("item_tooltip.the_last_sword.dragon_armor_leggings"));
            case FEET -> list.add(Component.translatable("item_tooltip.the_last_sword.dragon_armor_boots"));
        }

        // 全套效果描述
        list.add(Component.translatable("item_tooltip.the_last_sword.dragon_armor_skill"));
        list.add(Component.translatable("item_tooltip_lore.the_last_sword.dragon_armor")
            .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    // ==================== GeckoLib动画 ====================

    private PlayState predicate(AnimationState event) {
        if (this.animationprocedure.equals("empty")) {
            Entity entity = (Entity) event.getData(DataTickets.ENTITY);
            if (entity instanceof ArmorStand) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                return PlayState.CONTINUE;
            }

            Set<Item> wornArmor = new ObjectOpenHashSet<>();
            for (ItemStack stack : entity.getArmorSlots()) {
                if (stack.isEmpty())
                    return PlayState.STOP;
                wornArmor.add(stack.getItem());
            }

            boolean isWearingAll = wornArmor.containsAll(ObjectArrayList.of(
                    ModItems.DRAGON_ARMOR_BOOTS.get(),
                    ModItems.DRAGON_ARMOR_LEGGINGS.get(),
                    ModItems.DRAGON_ARMOR_CHESTPLATE.get(),
                    ModItems.DRAGON_ARMOR_HELMET.get()));

            if (!isWearingAll) {
                return PlayState.STOP;
            }

            if (entity instanceof LivingEntity livingEntity) {
                // 检查是否处于龙套提供的飞行状态
                boolean isDragonArmorFlying = false;
                if (livingEntity instanceof Player player) {
                    ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                    boolean wearingDragonChestplate = !chestplate.isEmpty() &&
                            chestplate.getItem() instanceof DragonArmorItem.Chestplate;
                    isDragonArmorFlying = wearingDragonChestplate && player.getAbilities().flying;
                }

                //根据飞行状态设置动画
                if (isDragonArmorFlying) {
                    //检查当前动画是否是fly，如果不是则切换
                    if (event.getController().getCurrentAnimation() == null ||
                        !event.getController().getCurrentAnimation().animation().name().equals("fly")) {
                        //切换到飞行动画
                        event.getController().setAnimation(RawAnimation.begin().thenLoop("fly"));
                    }
                } else {
                    //检查当前动画是否是idle，如果不是则切换
                    if (event.getController().getCurrentAnimation() != null &&
                        event.getController().getCurrentAnimation().animation().name().equals("fly")) {
                        //切换到待机动画
                        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                    } else if (event.getController().getCurrentAnimation() == null) {
                        //初始状态，直接设置idle动画
                        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
                    }
                }
            } else {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    String prevAnim = "empty";

    private PlayState procedurePredicate(AnimationState event) {
        if (!this.animationprocedure.equals("empty") &&
                event.getController().getAnimationState() == AnimationController.State.STOPPED ||
                (!this.animationprocedure.equals(prevAnim) && !this.animationprocedure.equals("empty"))) {

            if (!this.animationprocedure.equals(prevAnim))
                event.getController().forceAnimationReset();

            event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));

            if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                this.animationprocedure = "empty";
                event.getController().forceAnimationReset();
            }

            Entity entity = (Entity) event.getData(DataTickets.ENTITY);
            if (entity instanceof ArmorStand) {
                return PlayState.CONTINUE;
            }

            Set<Item> wornArmor = new ObjectOpenHashSet<>();
            for (ItemStack stack : entity.getArmorSlots()) {
                if (stack.isEmpty())
                    return PlayState.STOP;
                wornArmor.add(stack.getItem());
            }

            boolean isWearingAll = wornArmor.containsAll(ObjectArrayList.of(
                    ModItems.DRAGON_ARMOR_BOOTS.get(),
                    ModItems.DRAGON_ARMOR_LEGGINGS.get(),
                    ModItems.DRAGON_ARMOR_CHESTPLATE.get(),
                    ModItems.DRAGON_ARMOR_HELMET.get()));

            return isWearingAll ? PlayState.CONTINUE : PlayState.STOP;
        } else if (animationprocedure.equals("empty")) {
            prevAnim = "empty";
            return PlayState.STOP;
        }
        prevAnim = this.animationprocedure;
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "controller", 5, this::predicate));
        data.add(new AnimationController<>(this, "procedureController", 5, this::procedurePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== 能量系统 ====================

    //计算龙套的最大能量
    //公式：最大能量 = 基础值 + 等级 × 每级提升
    public static int getMaxEnergy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof DragonArmorItem)) {
            return 0;
        }
        int level = ItemLevelHelper.getLevel(stack);
        int perLevel = TheLastSwordConfiguration.getDragonArmorEnergyPerLevelSafely();
        return BASE_ENERGY + level * perLevel;
    }

    //检查实体是否穿戴全套龙之盔甲
    public static boolean isFullSet(LivingEntity entity) {
        return !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
                entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof DragonArmorItem.Helmet &&
                !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
                entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof DragonArmorItem.Chestplate &&
                !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
                entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof DragonArmorItem.Leggings &&
                !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty() &&
                entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof DragonArmorItem.Boots;
    }

    // ==================== 4个子类：头盔、胸甲、护腿、靴子 ====================

    public static class Helmet extends DragonArmorItem {
        public Helmet() {
            super(Type.HELMET, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/item/dragon_armor.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.HEAD;
        }
    }

    public static class Chestplate extends DragonArmorItem {
        public Chestplate() {
            super(Type.CHESTPLATE, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/item/dragon_armor.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.CHEST;
        }
    }

    public static class Leggings extends DragonArmorItem {
        public Leggings() {
            super(Type.LEGGINGS, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/item/dragon_armor.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.LEGS;
        }
    }

    public static class Boots extends DragonArmorItem {
        public Boots() {
            super(Type.BOOTS, new Properties());
        }

        @Override
        public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
            return "the_last_sword:textures/item/dragon_armor.png";
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.FEET;
        }
    }
}
