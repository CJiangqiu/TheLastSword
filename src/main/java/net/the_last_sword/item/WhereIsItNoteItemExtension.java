package net.the_last_sword.item;

import net.eca.api.RegisterItemExtension;
import net.eca.util.ItemUtil;
import net.eca.util.item_extension.ItemExtension;
import net.eca.util.item_extension.ItemExtensionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModItems;

//尖塔灵魂印记的深紫色乱码名称
@RegisterItemExtension
public class WhereIsItNoteItemExtension extends ItemExtension {

    static {
        ItemExtensionManager.register(new WhereIsItNoteItemExtension());
    }

    public WhereIsItNoteItemExtension() {
        super(ModItems.WHERE_IS_IT_NOTE.get());
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
    public MutableComponent getItemName(ItemStack stack) {
        return ItemUtil.of(Component.translatable("item.the_last_sword.where_is_it_note"))
            .addEffect.SOLID(0xAA00AA)
            .addEffect.GLITCH(0.05F)
            .build();
    }
}
