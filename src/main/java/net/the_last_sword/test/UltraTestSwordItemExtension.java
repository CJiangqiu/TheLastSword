package net.the_last_sword.test;

import net.eca.api.RegisterItemExtension;
import net.eca.client.render.TheLastEndRenderTypes;
import net.eca.util.item_extension.ItemExtension;
import net.eca.util.item_extension.ItemExtensionManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;

//究极测试剑物品扩展：全局封锁开关开启时套用终焉着色器
@RegisterItemExtension
public class UltraTestSwordItemExtension extends ItemExtension {

    static {
        ItemExtensionManager.register(new UltraTestSwordItemExtension());
    }

    public UltraTestSwordItemExtension() {
        super(ModItems.ULTRA_TEST_SWORD.get());
    }

    @Override
    protected String getModId() {
        return TheLastSwordMod.MOD_ID;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public boolean shouldRender(ItemStack stack) {
        return UltraTestSwordItem.isLockdownEnabled(stack);
    }

    @Override
    public RenderType getRenderType() {
        return TheLastEndRenderTypes.ITEM;
    }
}
