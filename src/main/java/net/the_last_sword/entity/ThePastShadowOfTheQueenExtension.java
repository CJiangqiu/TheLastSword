package net.the_last_sword.entity;

import net.eca.api.RegisterEntityExtension;
import net.eca.util.entity_extension.BossBarExtension;
import net.eca.util.entity_extension.CombatMusicExtension;
import net.eca.util.entity_extension.EntityExtension;
import net.eca.util.entity_extension.EntityExtensionManager;
import net.eca.util.entity_extension.EntityLayerExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.init.ModEntities;
import net.the_last_sword.init.ModSounds;

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

    @OnlyIn(Dist.CLIENT)
    @Override
    protected String getModId() {
        return TheLastSwordMod.MOD_ID;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BossBarExtension bossBarExtension() {
        return new BossBarExtension();
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
        if (entity instanceof ThePastShadowOfTheQueenEntity queen && queen.isReady() && queen.isAlive()) {
            return COMBAT_MUSIC;
        }
        return null;
    }
}
