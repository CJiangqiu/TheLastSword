package net.the_last_sword.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//龙晶魂石 - 用于存储剑灵的物品
public class DragonCrystalSoulStone extends Item {
    public DragonCrystalSoulStone() {
        super(new Item.Properties().rarity(Rarity.RARE).stacksTo(1).fireResistant());
    }

    //获取默认绑定的实体类型ID（子类可覆写）
    public String getDefaultBoundEntityId() {
        return null; //默认无绑定实体
    }

    //获取默认绑定实体的显示名称（子类可覆写）
    public String getDefaultBoundEntityDisplayName() {
        return null; //默认无绑定实体
    }

    //检查龙晶魂石是否存储了实体
    public static boolean hasStoredEntity(ItemStack stack) {
        if (stack.getItem() instanceof DragonCrystalSoulStone soulStone) {
            //检查是否有默认绑定实体
            if (soulStone.getDefaultBoundEntityId() != null) {
                return true;
            }
        }
        //检查NBT格式：wraith_entity_id键
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.contains("wraith_entity_id");
    }

    //获取存储实体的NBT数据
    public static CompoundTag getStoredEntity(ItemStack stack) {
        if (stack.getItem() instanceof DragonCrystalSoulStone soulStone) {
            //优先返回默认绑定实体
            String defaultEntityId = soulStone.getDefaultBoundEntityId();
            if (defaultEntityId != null) {
                CompoundTag defaultEntityData = new CompoundTag();
                defaultEntityData.putString("id", defaultEntityId);
                return defaultEntityData;
            }
        }

        //检查NBT格式：wraith_entity_id键
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("wraith_entity_id")) {
            CompoundTag entityData = new CompoundTag();
            entityData.putString("id", nbt.getString("wraith_entity_id"));
            return entityData;
        }

        return null;
    }

    //获取存储实体的显示名称（优先返回默认绑定）
    public static String getStoredEntityName(ItemStack stack) {
        if (stack.getItem() instanceof DragonCrystalSoulStone soulStone) {
            //优先返回默认绑定实体名称
            String defaultDisplayName = soulStone.getDefaultBoundEntityDisplayName();
            if (defaultDisplayName != null) {
                return defaultDisplayName;
            }
        }
        //否则从NBT获取实体显示名称
        CompoundTag entityData = getStoredEntity(stack);
        if (entityData != null) {
            String entityId = entityData.getString("id");
            if (!entityId.isEmpty()) {
                try {
                    ResourceLocation rl = new ResourceLocation(entityId);
                    EntityType<?> entityType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(rl);
                    if (entityType != null) {
                        return entityType.getDescriptionId();
                    }
                } catch (Exception ignored) {}
            }
        }
        return "";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        //始终显示龙晶魂石的状态信息
        if (hasStoredEntity(stack)) {
            String entityNameKey = getStoredEntityName(stack);
            if (!entityNameKey.isEmpty()) {
                tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.current_wraith")
                    .append(" ").append(Component.translatable(entityNameKey)));
            }
        } else {
            tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.current_wraith")
                .append(" ").append(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.empty")));
        }

        //显示是否召唤和UUID信息
        CompoundTag nbt = stack.getTag();
        Component summonedText;
        Component uuidText;

        if (nbt != null && nbt.contains("wraith_uuid")) {
            //有UUID数据
            boolean isSummoned = nbt.getBoolean("is_summoned");
            summonedText = Component.literal(String.valueOf(isSummoned));
            uuidText = Component.literal(nbt.getString("wraith_uuid"));
        } else {
            //没有数据，显示"空"
            summonedText = Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.empty");
            uuidText = Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.empty");
        }

        tooltip.add(Component.translatable("item_tooltip.the_last_sword.dragon_crystal_soul_stone.extra", summonedText, uuidText));
    }
}
