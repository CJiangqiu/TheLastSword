package net.the_last_sword.entity;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.the_last_sword.init.ModEntities;
import org.jetbrains.annotations.NotNull;

//龙之剑专用闪电实体 - 深紫色闪电视觉效果
public class DragonLightingEntity extends LightningBolt {
    private int life;
    public long seed;
    private int flashes;

    public DragonLightingEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.DRAGON_LIGHTING.get(), world);
    }

    public @NotNull SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public DragonLightingEntity(EntityType<DragonLightingEntity> type, Level world) {
        super(type, world);
        this.noCulling = true;
        this.life = 6;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(40) + 1;
        this.setVisualOnly(true);
    }
}
