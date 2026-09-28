package net.the_last_sword.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.the_last_sword.client.QueenExecutionCamera;
import net.the_last_sword.client.QueenTripleSlashScreenShake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void move(double forward, double up, double left);

    @Inject(method = "setup", at = @At("TAIL"))
    private void theLastSword$applyExecutionCamera(BlockGetter level, Entity entity,
            boolean detached, boolean mirror, float partialTick, CallbackInfo ci) {
        if (entity == Minecraft.getInstance().player && QueenExecutionCamera.isActive()) {
            setRotation(QueenExecutionCamera.getYaw(), 0.0F);
            move(0.0, 0.3, 0.0);
        }
        if (entity == Minecraft.getInstance().player && QueenTripleSlashScreenShake.isActive()) {
            Camera camera = (Camera) (Object) this;
            float yaw = camera.getYRot() + QueenTripleSlashScreenShake.getYawOffset(partialTick);
            float pitch = Mth.clamp(camera.getXRot()
                    + QueenTripleSlashScreenShake.getPitchOffset(partialTick), -90.0F, 90.0F);
            setRotation(yaw, pitch);
        }
    }
}
