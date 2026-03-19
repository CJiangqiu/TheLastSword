package net.the_last_sword.test;

import net.eca.api.RegisterEntityExtension;
import net.eca.client.render.TheLastEndRenderTypes;
import net.eca.util.entity_extension.BossBarExtension;
import net.eca.util.entity_extension.EntityExtension;
import net.eca.util.entity_extension.EntityExtensionManager;
import net.eca.util.entity_extension.EntityLayerExtension;
import net.eca.util.entity_extension.GlobalSkyboxExtension;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModEntities;

@SuppressWarnings("removal")
@RegisterEntityExtension
public class TestEntityExtension extends EntityExtension {

    private static final ResourceLocation TEST_ENTITY_TEXTURE =
        new ResourceLocation(TheLastSwordMod.MOD_ID, "textures/entities/test_entity.png");

    static {
        EntityExtensionManager.register(new TestEntityExtension());
    }

    public TestEntityExtension() {
        super(ModEntities.TEST_ENTITY.get(), 100);
    }

    @Override
    public boolean enableForceLoading() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BossBarExtension bossBarExtension() {
        return new BossBarExtension() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public RenderType getFrameRenderType() {
                return TheLastEndRenderTypes.BOSS_BAR;
            }

            @Override
            public RenderType getFillRenderType() {
                return TheLastEndRenderTypes.BOSS_BAR;
            }

            @Override
            public int getFrameWidth() {
                return 182;
            }

            @Override
            public int getFrameHeight() {
                return 18;
            }

            @Override
            public int getFillWidth() {
                return 182;
            }

            @Override
            public int getFillHeight() {
                return 18;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public EntityLayerExtension entityLayerExtension() {
        return new EntityLayerExtension() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public RenderType getRenderType() {
                return TheLastEndRenderTypes.createEntityEffect(TEST_ENTITY_TEXTURE);
            }

            @Override
            public boolean isGlow() {
                return true;
            }

            @Override
            public float getAlpha() {
                return 1.0f;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public GlobalSkyboxExtension globalSkyboxExtension() {
        return new GlobalSkyboxExtension() {
            @Override
            public boolean enabled() {
                return true;
            }

            @Override
            public boolean enableShader() {
                return true;
            }

            @Override
            public RenderType shaderRenderType() {
                return TheLastEndRenderTypes.SKYBOX;
            }

            @Override
            public float alpha() {
                return 1.0f;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected String getModId() {
        return TheLastSwordMod.MOD_ID;
    }
}
