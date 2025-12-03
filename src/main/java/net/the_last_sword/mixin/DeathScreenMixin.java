package net.the_last_sword.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.defence.DefenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {

    //取消死亡屏幕渲染（HEAD）
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void theLastSword$onDeathScreenRenderHead (GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            int level = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"), cancellable = true)
    private void theLastSword$onDeathScreenRenderTail (GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            int level = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
            if (level >= 2) {
                ci.cancel();
            }
        }
    }

    //死亡屏幕tick时立即关闭
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void theLastSword$onDeathScreenTickHead (CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            int level = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
            if (level >= 2) {
                ci.cancel();
                minecraft.setScreen(null);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void theLastSword$onDeathScreenTickTail (CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            int level = DefenceManager.hasDefenceRecord(player) ? DefenceManager.getDefenceLevel(player) : 0;
            if (level >= 2) {
                ci.cancel();
                minecraft.setScreen(null);
            }
        }
    }
}
