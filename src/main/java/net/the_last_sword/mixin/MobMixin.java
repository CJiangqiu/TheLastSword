package net.the_last_sword.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//防止可驯服实体和剑灵攻击盟友目标
@Mixin(Mob.class)
public class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingProtectedPlayersAndAllies(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (target == null) {
            return;
        }
        //判断盟友关系
        if (!EntityUtil.canAttack(mob, target)) {
            ci.cancel();
        }
    }
}
