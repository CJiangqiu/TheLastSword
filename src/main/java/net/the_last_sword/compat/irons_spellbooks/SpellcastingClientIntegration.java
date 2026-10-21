package net.the_last_sword.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.the_last_sword.init.ModItems;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class SpellcastingClientIntegration {
    private SpellcastingClientIntegration() {
    }

    public static void registerRenderers() {
        CuriosRendererRegistry.register(ModItems.DRAGON_CRYSTAL_GRIMOIRE.get(), SpellBookCurioRenderer::new);
    }
}
