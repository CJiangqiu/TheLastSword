package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    //阻止禁复活实体添加到世界
    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    private void onServerLevelAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = (ServerLevel)(Object)this;
        if (EntityUtil.isReviveBanned(level, entity.getType())) {
            cir.setReturnValue(false);
        }
    }

    //阻止禁复活实体添加到世界
    @Inject(at = @At("HEAD"), method = "addFreshEntity", cancellable = true)
    private void onServerLevelAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = (ServerLevel)(Object)this;
        if (EntityUtil.isReviveBanned(level, entity.getType())) {
            cir.setReturnValue(false);
        }
    }

    //世界tick处理 - 更新禁复活倒计时
    @Inject(at = @At("HEAD"), method = "tick")
    private void onServerLevelTickHead(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;
        //每秒更新禁复活倒计时
        if (level.getGameTime() % 20 == 0) {
            EntityUtil.tickReviveBans(level);
        }
    }

}
