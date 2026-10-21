package net.the_last_sword.mixin;

import net.minecraft.world.entity.animal.IronGolem;
import net.the_last_sword.entity.ai.IronGolemDragonCultTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void the_last_sword$targetWildDragonCult(CallbackInfo ci) {
        IronGolem golem = (IronGolem) (Object) this;
        // 与原版怪物索敌同级，保留村庄声望防卫和受击反击的优先级。
        golem.targetSelector.addGoal(3, new IronGolemDragonCultTargetGoal(golem));
    }
}
