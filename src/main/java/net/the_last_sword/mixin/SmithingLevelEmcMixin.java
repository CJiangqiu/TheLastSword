package net.the_last_sword.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.config.NBTProcessorConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.the_last_sword.compat.projecte.SmithingLevelEmcProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "moze_intel.projecte.emc.nbt.NBTManager", remap = false)
public abstract class SmithingLevelEmcMixin {
    @Shadow @Final
    private static List<INBTProcessor> processors;

    // 只有高阶形态有价值时，也要允许带附魔或名称的物品进入后续修正流程。
    @Inject(method = "getEmcValue", at = @At("HEAD"), cancellable = true, remap = false)
    private static void theLastSword$lookupLevel(ItemInfo info, CallbackInfoReturnable<Long> callback) {
        if (EMCMappingHandler.hasEmcValue(info)) {
            return;
        }
        ItemInfo normalized = SmithingLevelEmcProcessor.normalizeLevel(info);
        if (normalized == info) {
            return;
        }
        long emc = EMCMappingHandler.getStoredEmcValue(normalized);
        if (emc <= 0) {
            return;
        }
        // 使用等级基础价进入原处理器顺序，仍以完整标签计算耐久、附魔等修正。
        for (INBTProcessor processor : processors) {
            if (!NBTProcessorConfig.isEnabled(processor)) {
                continue;
            }
            try {
                emc = processor.recalculateEMC(info, emc);
            } catch (ArithmeticException exception) {
                callback.setReturnValue(emc);
                return;
            }
            if (emc <= 0) {
                callback.setReturnValue(0L);
                return;
            }
        }
        callback.setReturnValue(emc);
    }
}
