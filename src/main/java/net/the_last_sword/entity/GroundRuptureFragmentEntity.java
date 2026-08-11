package net.the_last_sword.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.the_last_sword.init.ModEntities;
import org.jetbrains.annotations.NotNull;

/** A short-lived, non-interactive block fragment used only by ground impact effects. */
public class GroundRuptureFragmentEntity extends Entity {
    private static final EntityDataAccessor<Integer> BLOCK_STATE_ID =
        SynchedEntityData.defineId(GroundRuptureFragmentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SCALE =
        SynchedEntityData.defineId(GroundRuptureFragmentEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VISUAL_SEED =
        SynchedEntityData.defineId(GroundRuptureFragmentEntity.class, EntityDataSerializers.INT);

    public static final int LIFETIME = 18;

    public GroundRuptureFragmentEntity(EntityType<? extends GroundRuptureFragmentEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public GroundRuptureFragmentEntity(Level level, BlockState blockState, Vec3 position,
                                       Vec3 velocity, float scale, int visualSeed) {
        this(ModEntities.GROUND_RUPTURE_FRAGMENT.get(), level);
        setBlockState(blockState);
        entityData.set(SCALE, scale);
        entityData.set(VISUAL_SEED, visualSeed);
        setPos(position.x, position.y, position.z);
        setDeltaMovement(velocity);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK_STATE_ID, Block.getId(Blocks.STONE.defaultBlockState()));
        entityData.define(SCALE, 0.5F);
        entityData.define(VISUAL_SEED, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= LIFETIME) {
            discard();
            return;
        }

        Vec3 movement = getDeltaMovement();
        move(MoverType.SELF, movement);
        setDeltaMovement(movement.x * 0.90D, movement.y - 0.045D, movement.z * 0.90D);
    }

    public BlockState getBlockState() {
        BlockState state = Block.stateById(entityData.get(BLOCK_STATE_ID));
        return state.isAir() ? Blocks.STONE.defaultBlockState() : state;
    }

    public float getFragmentScale() {
        return entityData.get(SCALE);
    }

    public int getVisualSeed() {
        return entityData.get(VISUAL_SEED);
    }

    private void setBlockState(BlockState state) {
        entityData.set(BLOCK_STATE_ID, Block.getId(state));
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        entityData.set(BLOCK_STATE_ID, tag.getInt("BlockState"));
        entityData.set(SCALE, tag.getFloat("Scale"));
        entityData.set(VISUAL_SEED, tag.getInt("VisualSeed"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("BlockState", entityData.get(BLOCK_STATE_ID));
        tag.putFloat("Scale", entityData.get(SCALE));
        tag.putInt("VisualSeed", entityData.get(VISUAL_SEED));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
