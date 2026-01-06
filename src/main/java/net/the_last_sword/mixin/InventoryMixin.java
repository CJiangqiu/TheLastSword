package net.the_last_sword.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.util.EntityQueryContext;
import net.the_last_sword.util.EntityUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

//背包清除防护
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Final
    @Shadow public Player player;

    /* ─────────── 高级方法拦截 ─────────── */

    @Inject(method = "clearOrCountMatchingItems",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockClear(Predicate<ItemStack> predicate,
                                int maxCount,
                                Container container,
                                CallbackInfoReturnable<Integer> cir) {
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            if (EntityUtil.hasProtection(this.player)) {
                cir.setReturnValue(0);
            }
        }
    }

    @Inject(method = "clearContent",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockClearContent(CallbackInfo ci) {
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            if (EntityUtil.hasProtection(this.player)) {
                ci.cancel();
            }
        }
    }

    /* ─────────── 底层方法拦截（使用调用栈判断）─────────── */

    @Inject(method = "setItem",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockSetItem(int slot, ItemStack stack, CallbackInfo ci) {
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            if (EntityUtil.hasProtection(this.player)) {
                //允许原版代码（GUI）和本mod自己的操作
                if (!EntityQueryContext.isAllowedToAccessProtectedEntities()) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "removeItemNoUpdate",
            at = @At("HEAD"), cancellable = true)
    private void tls$blockRemoveItemNoUpdate(int slot, CallbackInfoReturnable<ItemStack> cir) {
        if (TheLastSwordConfiguration.getDefenceEnableRadicalLogicSafely()) {
            if (EntityUtil.hasProtection(this.player)) {
                if (!EntityQueryContext.isAllowedToAccessProtectedEntities()) {
                    cir.setReturnValue(ItemStack.EMPTY);
                }
            }
        }
    }
}
