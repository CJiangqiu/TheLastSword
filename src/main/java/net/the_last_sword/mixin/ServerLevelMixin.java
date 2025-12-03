package net.the_last_sword.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.attack.AttackManager;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.util.EntityQueryContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    //阻止禁复活实体添加到世界
    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    private void onServerLevelAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Integer remainingTime = AttackManager.getAllReviveBanTypes().get(entity.getClass());
        if (remainingTime != null && remainingTime > 0) {
            cir.setReturnValue(false);
        }
    }

    //阻止禁复活实体添加到世界
    @Inject(at = @At("HEAD"), method = "addFreshEntity", cancellable = true)
    private void onServerLevelAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Integer remainingTime = AttackManager.getAllReviveBanTypes().get(entity.getClass());
        if (remainingTime != null && remainingTime > 0) {
            cir.setReturnValue(false);
        }
    }

    //世界tick处理
    @Inject(at = @At("HEAD"), method = "tick")
    private void onServerLevelTickHead(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;
        DefenceManager.tick(level.getServer(), level.getGameTime());
        AttackManager.worldTick(level);

    }

}
