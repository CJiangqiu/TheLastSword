package net.the_last_sword.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.item.TheLastSword;
import net.the_last_sword.util.nbt.ItemModeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockBehaviourMixin {

    //超级破坏：最终之剑挖掘模式下允许左键挖掘不可破坏方块
    @Inject(
        method = "getDestroyProgress",
        at = @At("HEAD"),
        cancellable = true
    )
    private void theLastSword$superDestroyProgress(Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!TheLastSwordConfiguration.getSuperDestroySafely()) return;

        BlockBehaviour.BlockStateBase self = (BlockBehaviour.BlockStateBase) (Object) this;
        if (self.getDestroySpeed(level, pos) >= 0) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof TheLastSword)) return;
        if (ItemModeHelper.getMode(held) != 1) return;

        float speed = held.getDestroySpeed(self.getBlock().defaultBlockState());
        cir.setReturnValue(speed / 100f);
    }

    //虚化效果：修改方块碰撞箱，实现穿墙
    @Inject(
        method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void theLastSword$phasingCollision(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return;
        }

        var entity = entityContext.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (!livingEntity.hasEffect(ModEffects.PHASING.get())) {
            return;
        }

        //虚化实体处理碰撞
        if (livingEntity instanceof Player player && player.getAbilities().flying) {
            //飞行中：移除所有方块碰撞（完全虚化，可以向下穿透地面）
            cir.setReturnValue(Shapes.empty());
        } else {
            //非飞行：只保留脚下方块碰撞（站在地面上）
            double entityY = livingEntity.getY();
            int entityBlockY = (int) Math.floor(entityY);

            //只保留实体下方的方块碰撞（让实体站在地面上）
            //移除实体当前位置以上的方块碰撞
            if (pos.getY() >= entityBlockY) {
                cir.setReturnValue(Shapes.empty());
            }
        }
    }
}
