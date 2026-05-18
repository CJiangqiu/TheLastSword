package net.the_last_sword.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.configuration.TheLastSwordConfiguration;
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

    //拦截按条件清除物品（/clear等）
    @Inject(method = "clearOrCountMatchingItems", at = @At("HEAD"), cancellable = true)
    private void tls$blockClear(Predicate<ItemStack> predicate, int maxCount, Container container, CallbackInfoReturnable<Integer> cir) {
        if (EntityUtil.hasInventoryProtection(this.player)) {
            cir.setReturnValue(0);
        }
    }

    //拦截清空背包
    @Inject(method = "clearContent", at = @At("HEAD"), cancellable = true)
    private void tls$blockClearContent(CallbackInfo ci) {
        if (EntityUtil.hasInventoryProtection(this.player)) {
            ci.cancel();
        }
    }

    //拦截死亡时清空并掉落背包（dropAll 在 drop 后直接 list.set EMPTY，不走 setItem，必须在此拦截）
    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void tls$blockDropAll(CallbackInfo ci) {
        if (EntityUtil.hasInventoryProtection(this.player)) {
            ci.cancel();
        }
    }

    //拦截按引用移除物品（仅拦截外部mod调用，原版操作放行；受强力背包保护开关控制）
    @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void tls$blockRemoveItemByReference(ItemStack stack, CallbackInfo ci) {
        if (TheLastSwordConfiguration.getEnableStrongInventoryProtectionSafely()
                && EntityUtil.hasInventoryProtection(this.player)
                && net.eca.util.EntityUtil.hasExternalCaller(5)) {
            ci.cancel();
        }
    }

    //拦截按槽位设置物品（仅拦截外部mod调用，防止强制缴械或替换；受强力背包保护开关控制）
    @Inject(method = "setItem(ILnet/minecraft/world/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void tls$blockSetItem(int index, ItemStack stack, CallbackInfo ci) {
        if (TheLastSwordConfiguration.getEnableStrongInventoryProtectionSafely()
                && EntityUtil.hasInventoryProtection(this.player)
                && net.eca.util.EntityUtil.hasExternalCaller(5)) {
            ci.cancel();
        }
    }
}
