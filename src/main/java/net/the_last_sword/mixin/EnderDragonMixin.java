package net.the_last_sword.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.the_last_sword.event.EnderDragonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EnderDragon.class, priority = 1024)
public abstract class EnderDragonMixin {

    @ModifyArg(
            method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float theLastSword$applyNamedFlyingSpeed(float acceleration) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        AttributeInstance flyingSpeedAttribute = dragon.getAttribute(Attributes.FLYING_SPEED);
        if (flyingSpeedAttribute == null || flyingSpeedAttribute.getBaseValue() <= 0.0) {
            return acceleration;
        }
        double baseSpeed = flyingSpeedAttribute.getBaseValue();
        double flyingSpeed = flyingSpeedAttribute.getValue();
        return (float) (acceleration * Math.max(0.0, flyingSpeed / baseSpeed));
    }

    @ModifyConstant(method = "knockBack(Ljava/util/List;)V", constant = @Constant(floatValue = 5.0F))
    private float theLastSword$scaleWingDamage(float originalDamage) {
        return EnderDragonEvent.scaleDragonContactDamage((EnderDragon) (Object) this, originalDamage);
    }

    @ModifyConstant(method = "hurt(Ljava/util/List;)V", constant = @Constant(floatValue = 10.0F))
    private float theLastSword$scaleHeadDamage(float originalDamage) {
        return EnderDragonEvent.scaleDragonContactDamage((EnderDragon) (Object) this, originalDamage);
    }
}
