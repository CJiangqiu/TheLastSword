package net.the_last_sword.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.the_last_sword.entity.TheLastEndEntity;
import net.the_last_sword.entity.TheLastEndSwordWraithEntity;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.util.EntityUtil;
import org.jetbrains.annotations.NotNull;

//13级终焉剑灵生成蛋
public class TheLastEndSwordWraithLevel13SpawnEgg extends Item {

    public TheLastEndSwordWraithLevel13SpawnEgg() {
        super(new Item.Properties()
                .stacksTo(16)
                .rarity(Rarity.EPIC)
                .fireResistant()
        );
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        //获取点击位置和方向
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(clickedFace);

        //检查生成位置是否有足够空间
        if (!level.getBlockState(spawnPos).isAir() && !level.getBlockState(spawnPos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        //在服务端生成实体
        if (level instanceof ServerLevel serverLevel) {
            TheLastEndSwordWraithEntity wraith = ModEntities.THE_LAST_END_SWORD_WRAITH.get().create(serverLevel);
            if (wraith != null) {
                //设置生成位置（在方块上方）
                double x = spawnPos.getX() + 0.5;
                double y = spawnPos.getY();
                double z = spawnPos.getZ() + 0.5;
                wraith.moveTo(x, y, z, context.getRotation(), 0.0F);

                //设置等级为13
                wraith.setTheLastEndLevel(13);

                //终焉种初始化：世界锚、防御注册、生成动画
                float maxHealth = (float) wraith.getAttributeValue(Attributes.MAX_HEALTH);
                wraith.setWorldAnchorMax(maxHealth);
                wraith.setWorldAnchor(maxHealth);
                if (!EntityUtil.hasProtection(wraith)) {
                    EntityUtil.registerDefence(wraith, maxHealth);
                }
                wraith.setAnimationState(TheLastEndEntity.STATE_SPAWNING);

                //设置朝向玩家
                if (context.getPlayer() != null) {
                    wraith.yBodyRot = context.getPlayer().getYRot();
                    wraith.yHeadRot = context.getPlayer().getYRot();
                }

                //添加到世界
                serverLevel.addFreshEntity(wraith);

                //播放音效
                level.playSound(null, spawnPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 1.0F, 1.0F);

                //触发游戏事件
                level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);

                //非创造模式下减少物品
                if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.FAIL;
    }
}
