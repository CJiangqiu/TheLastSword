package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.the_last_sword.defence.DefenceManager;
import net.the_last_sword.init.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    //3级防御玩家隐身渲染，但虚化状态例外（虚化有半透明渲染）
    @Inject(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void theLastSword$render(AbstractClientPlayer player, float entityYaw, float partialTicks,
                                     PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                     CallbackInfo ci) {
        int level = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
        if (level != 3) {
            return;
        }

        //3级防御玩家取消渲染，但虚化状态例外（虚化有半透明渲染）
        if (!player.hasEffect(ModEffects.PHASING.get())) {
            ci.cancel();
        }
    }
}
