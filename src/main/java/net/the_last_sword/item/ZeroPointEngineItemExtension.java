package net.the_last_sword.item;

import net.eca.api.RegisterItemExtension;
import net.eca.client.render.ShaderMaskPass;
import net.eca.util.item_extension.ItemExtension;
import net.eca.util.item_extension.ItemExtensionManager;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.client.shader.ZeroPointEngineShader;
import net.the_last_sword.init.ModItems;

import java.util.List;

@RegisterItemExtension
public class ZeroPointEngineItemExtension extends ItemExtension {
    private static final List<ShaderMaskPass> PASSES = List.of(
            ShaderMaskPass.unmasked(ZeroPointEngineShader.ITEM, 1.0F));

    static {
        ItemExtensionManager.register(new ZeroPointEngineItemExtension());
    }

    public ZeroPointEngineItemExtension() {
        super(ModItems.ZERO_POINT_ENGINE.get());
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
    public List<ShaderMaskPass> getShaderPasses() {
        return PASSES;
    }
}
