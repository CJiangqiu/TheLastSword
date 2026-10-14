package net.the_last_sword.compat.projecte;

import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public interface PrimalSourceArmorItem extends GeoItem {
    RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    // 独立方法名避免继承实现重混淆后与接口声明失配。
    EquipmentSlot getPrimalArmorSlot();

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE)));
    }
}
