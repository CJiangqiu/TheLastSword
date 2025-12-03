package net.the_last_sword.entity;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import net.the_last_sword.init.ModEntities;
import org.jetbrains.annotations.NotNull;

//最终之剑专用闪电实体 - 末地传送门材质视觉效果
public class TheLastEndLightingEntity extends LightningBolt {
    private int life;
    public long seed;
    private int flashes;

    public TheLastEndLightingEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.THE_LAST_END_LIGHTING.get(), world);
    }

    public @NotNull SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    public TheLastEndLightingEntity(EntityType<TheLastEndLightingEntity> type, Level world) {
        super(type, world);
        this.noCulling = true;
        this.life = 6;
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(40) + 1;
        this.setVisualOnly(true);
    }
}
