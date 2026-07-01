package net.the_last_sword.item;

import net.eca.api.RegisterItemExtension;
import net.eca.client.render.TheLastEndRenderTypes;
import net.eca.util.ItemUtil;
import net.eca.util.item_extension.ItemExtension;
import net.eca.util.item_extension.ItemExtensionManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;

// 隐藏武器物品扩展：红色名字 + 乱码 + 加粗
@RegisterItemExtension
public class TheLastSwordYouNeverForgotItemExtension extends ItemExtension {

    static {
        ItemExtensionManager.register(new TheLastSwordYouNeverForgotItemExtension());
    }

    public TheLastSwordYouNeverForgotItemExtension() {
        super(ModItems.THE_LAST_SWORD_YOU_NEVER_FORGOT.get());
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
    public RenderType getRenderType() {
        return TheLastEndRenderTypes.ITEM;
    }

    @Override
    public float[] getColorKey() {
        int rgb = 0x000000;
        return new float[]{((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f};
    }

    @Override
    public float getColorKeyTolerance() {
        return 0.1f;
    }

    // 红色名字 + 乱码效果 + 加粗
    @Override
    public MutableComponent getItemName(ItemStack stack) {
        return ItemUtil.of(Component.translatable("item.the_last_sword.the_last_sword_you_never_forgot"))
                .addEffect.SOLID(0xFF0000)
                .addEffect.GLITCH(0.05f)
                .addEffect.BOLD()
                .build();
    }
}
