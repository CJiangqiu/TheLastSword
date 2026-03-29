package net.the_last_sword.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;

    //受保护玩家直接将死亡界面替换为null
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen theLastSword$preventDeathScreen(Screen screen) {
        if (screen instanceof DeathScreen && this.player != null && EntityUtil.hasProtection(this.player)) {
            return null;
        }
        return screen;
    }
}
