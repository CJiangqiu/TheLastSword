package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.the_last_sword.init.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    //虚化效果：阻止渲染屏幕效果（方块覆盖纹理），实现透视效果
    @Inject(
        method = "renderScreenEffect",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void theLastSword$cancelScreenEffectWhenPhasing(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
        if (minecraft.player == null) {
            return;
        }

        //如果玩家有虚化效果，完全取消屏幕效果渲染（方块覆盖、水下、火焰等）
        if (minecraft.player.hasEffect(ModEffects.PHASING.get())) {
            ci.cancel();
        }
    }
}
