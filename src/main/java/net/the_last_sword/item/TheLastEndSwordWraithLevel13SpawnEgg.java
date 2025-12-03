package net.the_last_sword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEntities;

//13级终焉剑灵召唤物品
public class TheLastEndSwordWraithLevel13SpawnEgg extends Item {

    public TheLastEndSwordWraithLevel13SpawnEgg() {
        super(new Properties()
                .stacksTo(64)
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = pos.relative(direction);

        //创建13级终焉剑灵
        TheLastEndSwordWraithEntity wraith = ModEntities.THE_LAST_END_SWORD_WRAITH.get().create(serverLevel);
        if (wraith != null) {
            //设置位置
            wraith.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);

            //设置等级为13
            wraith.setEndLevel(13);

            //设置已生成状态
            wraith.setIsSpawned(true);

            //添加到世界
            serverLevel.addFreshEntity(wraith);

            //消耗物品
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}
