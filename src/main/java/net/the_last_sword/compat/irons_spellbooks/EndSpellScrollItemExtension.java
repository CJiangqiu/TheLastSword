package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.eca.client.render.ShaderMaskPass;
import net.eca.client.render.TheLastEndRenderTypes;
import net.eca.util.item_extension.ItemExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.the_last_sword.TheLastSwordMod;

import java.util.List;

public final class EndSpellScrollItemExtension extends ItemExtension {
    private static final ResourceLocation SCHOOL_ID =
        ResourceLocation.fromNamespaceAndPath(TheLastSwordMod.MOD_ID, "the_last_end");
    private static final List<ShaderMaskPass> PASSES = List.of(
        ShaderMaskPass.baseTexture(TheLastEndRenderTypes.ITEM, 0xEA4EEF, 0.01F, 1.0F),
        ShaderMaskPass.baseTexture(TheLastEndRenderTypes.ITEM, 0xBE3DD5, 0.01F, 1.0F),
        ShaderMaskPass.baseTexture(TheLastEndRenderTypes.ITEM, 0x952AB1, 0.01F, 1.0F)
    );

    public EndSpellScrollItemExtension() {
        super(ItemRegistry.SCROLL.get());
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
        // 卷轴共用物品注册项，必须按当前物品栈中的学派筛选。
        ISpellContainer container = ISpellContainer.get(stack);
        if (container == null || container.isEmpty()) {
            return false;
        }
        SpellData data = container.getSpellAtIndex(0);
        return !data.equals(SpellData.EMPTY) && SCHOOL_ID.equals(data.getSpell().getSchoolType().getId());
    }

    @Override
    public List<ShaderMaskPass> getShaderPasses() {
        return PASSES;
    }
}
