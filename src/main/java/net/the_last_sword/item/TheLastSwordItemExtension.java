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

//最终之剑物品扩展：交由 ECA 终焉着色器渲染剑刃，并提供紫色闪烁加粗剑名
@RegisterItemExtension
public class TheLastSwordItemExtension extends ItemExtension {

    static {
        ItemExtensionManager.register(new TheLastSwordItemExtension());
    }

    public TheLastSwordItemExtension() {
        super(ModItems.THE_LAST_SWORD.get());
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

    //剑名：紫色纯色 + 闪烁 + 加粗
    @Override
    public MutableComponent getItemName(ItemStack stack) {
        return ItemUtil.of(Component.translatable("item.the_last_sword.the_last_sword"))
                .addEffect.SOLID(0x9447EB)
                .addEffect.SHIMMER(0.2f)
                .addEffect.BOLD()
                .build();
    }
}
