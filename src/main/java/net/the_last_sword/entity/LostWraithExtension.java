package net.the_last_sword.entity;

import net.the_last_sword.util.health.TrueHealthManager;

import net.eca.api.RegisterEntityExtension;
import net.eca.client.render.ArcaneRenderTypes;
import net.eca.client.render.TheLastEndRenderTypes;
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
import net.the_last_sword.configuration.TheLastSwordConfiguration;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModSounds;

// 迷失战魂ECA实体扩展 - 终焉风格Boss血条
@RegisterEntityExtension
public class LostWraithExtension extends EntityExtension {
    private static final CombatMusicExtension COMBAT_MUSIC = new CombatMusicExtension() {
        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public ResourceLocation soundEventId() {
            return ModSounds.LOST_WRAITH.getId();
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
        EntityExtensionManager.register(new LostWraithExtension());
    }

    public LostWraithExtension() {
        super(ModEntities.LOST_WRAITH.get(), 5);
    }

    @Override
    public boolean enableBossBar() {
        return true;
    }

    @Override
    public boolean shouldShowBossBar(LivingEntity entity) {
        return entity instanceof LostWraithEntity lw && lw.isAlive()
            && lw.getAnimationState() != TheLastEndEntity.STATE_UNSPAWNED && !lw.isDying();
    }

    @Override
    public boolean enableCustomHealthOverride() {
        return true;
    }

    @Override
    public Number getCustomHealthValue(LivingEntity entity) {
        return entity instanceof LostWraithEntity lw ? TrueHealthManager.getHealth(lw) : null;
    }

    @Override
    public boolean enableCustomMaxHealthOverride() {
        return true;
    }

    @Override
    public Number getCustomMaxHealthValue(LivingEntity entity) {
        return entity instanceof LostWraithEntity lw ? TrueHealthManager.getMaxHealth(lw) : null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected String getModId() {
        return "the_last_sword";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BossBarExtension bossBarExtension() {
        return new BossBarExtension() {
            @Override
            public boolean enabled() {
                return TheLastSwordConfiguration.getLostWraithEnableCustomBossBarSafely();
            }

            @Override
            public RenderType getFillRenderType() {
                return TheLastEndRenderTypes.BOSS_BAR;
            }

            @Override
            public int getFillWidth() {
                return 182;
            }

            @Override
            public int getFillHeight() {
                return 10;
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
        if (entity instanceof LostWraithEntity wraith && wraith.isAlive()
                && wraith.getAnimationState() != TheLastEndEntity.STATE_UNSPAWNED && !wraith.isDying()) {
            return COMBAT_MUSIC;
        }
        return null;
    }
}
