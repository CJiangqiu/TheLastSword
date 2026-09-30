package net.the_last_sword.entity;

import net.eca.api.RegisterEntityExtension;
import net.eca.client.render.DreamSakuraRenderTypes;
import net.eca.util.entity_extension.BossBarExtension;
import net.eca.util.entity_extension.CombatMusicExtension;
import net.eca.util.entity_extension.EntityExtension;
import net.eca.util.entity_extension.EntityExtensionManager;
import net.eca.util.entity_extension.EntityLayerExtension;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModSounds;
import net.the_last_sword.util.health.TrueHealthManager;

@RegisterEntityExtension
public class ThePastShadowOfTheQueenExtension extends EntityExtension {
    private static final CombatMusicExtension COMBAT_MUSIC = new CombatMusicExtension() {
        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public ResourceLocation soundEventId() {
            return ModSounds.THE_PAST_SHADOW_OF_THE_QUEEN.getId();
        }

        @Override
        public SoundSource soundSource() {
            return SoundSource.MUSIC;
        }

        @Override
        public float volume() {
            return 1.0F;
        }

        @Override
        public float pitch() {
            return 1.0F;
        }

        @Override
        public boolean loop() {
            return true;
        }

        @Override
        public boolean strictMusicLock() {
            return true;
        }
    };

    static {
        EntityExtensionManager.register(new ThePastShadowOfTheQueenExtension());
    }

    public ThePastShadowOfTheQueenExtension() {
        super(ModEntities.THE_PAST_SHADOW_OF_THE_QUEEN.get(), 6);
    }

    @Override
    public boolean enableBossBar() {
        return true;
    }

    @Override
    public boolean shouldShowBossBar(LivingEntity entity) {
        return entity instanceof ThePastShadowOfTheQueenEntity queen
                && queen.isReady() && queen.isAlive() && !queen.isDying() && !queen.isNpc();
    }

    @Override
    public boolean enableCustomHealthOverride() {
        return true;
    }

    @Override
    public Number getCustomHealthValue(LivingEntity entity) {
        return TrueHealthManager.getHealth(entity);
    }

    @Override
    public boolean enableCustomMaxHealthOverride() {
        return true;
    }

    @Override
    public Number getCustomMaxHealthValue(LivingEntity entity) {
        return TrueHealthManager.getMaxHealth(entity);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected String getModId() {
        return TheLastSwordMod.MOD_ID;
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
            public RenderType getFillRenderType() {
                return DreamSakuraRenderTypes.BOSS_BAR;
            }

            // 数值文字按外框高度居中；透明外框为纯填充血条提供一致的布局尺寸。
            @Override
            public RenderType getFrameRenderType() {
                return DreamSakuraRenderTypes.BOSS_BAR;
            }

            @Override
            public float getFrameAlpha() {
                return 0.0F;
            }

            @Override
            public int getFrameWidth() {
                return getFillWidth();
            }

            @Override
            public int getFrameHeight() {
                return getFillHeight();
            }

            @Override
            public int getFillWidth() {
                return 182;
            }

            @Override
            public int getFillHeight() {
                return 10;
            }

            @Override
            public boolean showValueText() {
                return true;
            }

            @Override
            public Number getDisplayCurrentValue(LivingEntity entity) {
                return TrueHealthManager.getHealth(entity);
            }

            @Override
            public Number getDisplayMaxValue(LivingEntity entity) {
                return TrueHealthManager.getMaxHealth(entity);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public EntityLayerExtension entityLayerExtension() {
        return new EntityLayerExtension();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public CombatMusicExtension combatMusicExtension() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public CombatMusicExtension combatMusicExtension(LivingEntity entity) {
        if (entity instanceof ThePastShadowOfTheQueenEntity queen
                && queen.isReady() && !queen.isNpc() && queen.isAlive()) {
            return COMBAT_MUSIC;
        }
        return null;
    }
}
