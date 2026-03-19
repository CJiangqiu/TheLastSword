package net.the_last_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.the_last_sword.test.UltraTestSwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    //防御模式：禁止受伤摄像机晃动
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void theLastSword$cancelBobHurt(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && UltraTestSwordItem.hasDefenseSword(player)) {
            ci.cancel();
        }
    }

    //防御模式：禁止行走视角摇摆
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void theLastSword$cancelBobView(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && UltraTestSwordItem.hasDefenseSword(player)) {
            ci.cancel();
        }
    }

    //防御模式：禁止恶心效果画面扭曲
    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void theLastSword$cancelConfusionOverlay(GuiGraphics guiGraphics, float strength, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null && UltraTestSwordItem.hasDefenseSword(player)) {
            ci.cancel();
        }
    }
}
