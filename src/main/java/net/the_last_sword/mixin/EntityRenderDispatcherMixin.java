package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.the_last_sword.client.renderer.PhasingBufferSource;
import net.the_last_sword.util.PhasingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    // 在模型提交顶点前统一处理，避免模型调用被重定向后参数索引发生变化。
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private MultiBufferSource theLastSword$phasingBuffers(MultiBufferSource original,
            Entity entity, double x, double y, double z, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int light) {
        if (entity instanceof LivingEntity living && PhasingState.isPhasing(living)) {
            return PhasingBufferSource.wrap(original);
        }
        return original;
    }
}
