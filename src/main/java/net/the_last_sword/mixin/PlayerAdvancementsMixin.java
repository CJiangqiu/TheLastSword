package net.the_last_sword.mixin;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.the_last_sword.event.TheLastSwordQuestHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//在原版写入前拦截，避免关闭任务系统后短暂完成进度或发出提示
@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Inject(method = "award", at = @At("HEAD"), cancellable = true)
    private void tls$blockMainQuestAdvancement(Advancement advancement, String criterion,
            CallbackInfoReturnable<Boolean> cir) {
        if (TheLastSwordQuestHandler.shouldBlockMainQuestAdvancement(advancement.getId())) {
            cir.setReturnValue(false);
        }
    }
}
