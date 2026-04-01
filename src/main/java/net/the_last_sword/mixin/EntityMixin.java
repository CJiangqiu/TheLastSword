package net.the_last_sword.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.the_last_sword.client.PerceptionScanData;
import net.the_last_sword.init.ModEffects;
import net.the_last_sword.summon.WraithSummonManager;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;
import javax.annotation.Nullable;

@Mixin(value = Entity.class, priority = 1024)
public class EntityMixin {

    @Shadow @Nullable private Entity.RemovalReason removalReason;

    @Inject(method = "kill", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventKill(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (self instanceof LivingEntity livingEntity) {
            if (EntityUtil.hasProtection(livingEntity)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "discard", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventDiscard(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (self instanceof LivingEntity livingEntity) {
            if (EntityUtil.hasProtection(livingEntity)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (reason == Entity.RemovalReason.CHANGED_DIMENSION) return;
        if (self instanceof LivingEntity living && EntityUtil.hasProtection(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        if (reason == Entity.RemovalReason.CHANGED_DIMENSION) return;
        if (self instanceof LivingEntity living && EntityUtil.hasProtection(living)) {
            ci.cancel();
        }
    }

    @Inject(method = "setLevelCallback", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventSetLevelCallback(EntityInLevelCallback callback, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide) return;
        Entity.RemovalReason reason = self.getRemovalReason();
        if (reason == Entity.RemovalReason.CHANGED_DIMENSION) return;
        if (self instanceof LivingEntity living && EntityUtil.hasProtection(living)) {
            if (callback == EntityInLevelCallback.NULL) {
                ci.cancel();
            }
        }
    }

    //防止受保护实体因 removalReason 残留导致 tick 跳过、@e 选择器失效（仅服务端）
    @Inject(method = "isRemoved", at = @At("HEAD"), cancellable = true)
    private void theLastSword$preventRemovedState(CallbackInfoReturnable<Boolean> cir) {
        if (this.removalReason != null
                && this.removalReason != Entity.RemovalReason.CHANGED_DIMENSION
                && (Object) this instanceof LivingEntity living
                && !living.level().isClientSide
                && EntityUtil.hasProtection(living)) {
            this.removalReason = null;
            cir.setReturnValue(false);
        }
    }

    //虚化效果：不判定为在墙中
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void theLastSword$phasingNotInWall(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity livingEntity) {
            if (livingEntity.hasEffect(ModEffects.PHASING.get())) {
                cir.setReturnValue(false);
            }
        }
    }

    //剑灵系统盟友判断
    @Inject(method = "isAlliedTo", at = @At("HEAD"), cancellable = true)
    private void theLastSword$checkWraithAllied(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        //情况1：自己是剑灵 - 检查对方是否是主人或主人的盟友
        if (self instanceof LivingEntity living && WraithSummonManager.isWraith(living)) {
            Player owner = WraithSummonManager.getOwner(living, self.level());
            if (owner != null) {
                //不攻击主人
                if (other == owner) {
                    cir.setReturnValue(true);
                    return;
                }
                //不攻击主人的盟友
                if (EntityUtil.areOriginalAllies(owner, other)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        //情况2：对方是剑灵 - 检查自己是否是对方主人或主人的盟友
        if (other instanceof LivingEntity living && WraithSummonManager.isWraith(living)) {
            Player owner = WraithSummonManager.getOwner(living, other.level());
            if (owner != null) {
                //主人不攻击剑灵
                if (self == owner) {
                    cir.setReturnValue(true);
                    return;
                }
                //主人的盟友不攻击剑灵
                if (EntityUtil.areOriginalAllies(owner, self)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    //感知模块：客户端强制发光
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void theLastSword$perceptionGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide() && PerceptionScanData.isScanned(self.getId())) {
            cir.setReturnValue(true);
        }
    }

    //感知模块：客户端自定义描边颜色
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void theLastSword$perceptionTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide() && PerceptionScanData.isScanned(self.getId())) {
            cir.setReturnValue(PerceptionScanData.getColor(self.getId()));
        }
    }
}
