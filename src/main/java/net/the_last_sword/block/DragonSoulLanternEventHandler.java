package net.the_last_sword.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.the_last_sword.init.ModBlocks;
import net.the_last_sword.item.DragonCrystalSoulStone;

//龙魂灯笼事件处理器 - 处理灵魂收集功能
@Mod.EventBusSubscriber(modid = "the_last_sword")
public class DragonSoulLanternEventHandler {

    private static final double LANTERN_RANGE = 8.0;

    //实体死亡时检查龙魂灯笼范围并存储到魂石
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        Level level = event.getEntity().level();
        BlockPos deathPos = event.getEntity().blockPosition();

        if (!hasNearbyDragonSoulLantern(level, deathPos)) {
            return;
        }

        ItemStack emptySoulStone = findEmptySoulStone(player);
        if (emptySoulStone.isEmpty()) {
            return;
        }

        ResourceLocation entityId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if (entityId != null) {
            CompoundTag nbt = emptySoulStone.getOrCreateTag();
            nbt.putString("wraith_entity_id", entityId.toString());

            //保存实体的完整NBT数据（包括装备、属性、自定义名称等）
            CompoundTag entityNBT = new CompoundTag();
            event.getEntity().save(entityNBT);

            //修正血量为最大生命值（避免保存死亡时的0血）
            float maxHealth = event.getEntity().getMaxHealth();
            entityNBT.putFloat("Health", maxHealth);

            nbt.put("entity_nbt", entityNBT);

            String entityName = event.getEntity().hasCustomName() ?
                event.getEntity().getCustomName().getString() :
                event.getEntity().getDisplayName().getString();

            player.sendSystemMessage(Component.translatable(
                "message.the_last_sword.dragon_soul_lantern.stored_success", entityName));
        }
    }

    //检查附近是否有激活的龙魂灯笼（底部必须有黑曜石或哭泣的黑曜石）
    private static boolean hasNearbyDragonSoulLantern(Level level, BlockPos center) {
        int range = (int) Math.ceil(LANTERN_RANGE);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = center.offset(x, y, z);

                    if (center.distSqr(checkPos) <= LANTERN_RANGE * LANTERN_RANGE) {
                        if (level.getBlockState(checkPos).getBlock() == ModBlocks.DRAGON_SOUL_LANTERN.get()) {
                            if (isLanternActivated(level, checkPos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    //检查龙魂灯笼是否激活（底部是否有黑曜石或哭泣的黑曜石）
    private static boolean isLanternActivated(Level level, BlockPos lanternPos) {
        BlockPos belowPos = lanternPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.is(Blocks.OBSIDIAN) || belowState.is(Blocks.CRYING_OBSIDIAN);
    }

    //查找玩家背包中的空魂石
    private static ItemStack findEmptySoulStone(Player player) {
        if (player.getMainHandItem().getItem() instanceof DragonCrystalSoulStone) {
            ItemStack stack = player.getMainHandItem();
            if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                return stack;
            }
        }

        if (player.getOffhandItem().getItem() instanceof DragonCrystalSoulStone) {
            ItemStack stack = player.getOffhandItem();
            if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                return stack;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof DragonCrystalSoulStone) {
                if (!DragonCrystalSoulStone.hasStoredEntity(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
