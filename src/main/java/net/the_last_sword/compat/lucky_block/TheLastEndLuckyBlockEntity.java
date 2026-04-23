package net.the_last_sword.compat.lucky_block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.the_last_sword.init.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

//终焉幸运方块实体 - 承载GeckoLib循环动画 + luck值NBT持久化
public class TheLastEndLuckyBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final String TAG_LUCK = "Luck";
    private static final String TAG_WELL_VARIANT = "WellVariant";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //幸运方块循环动画（环旋转 + 光柱脉动）
    private static final RawAnimation LOOP = RawAnimation.begin().thenLoop("0");

    //幸运值：正偏向好事件，负偏向坏事件
    private int luck = 0;
    //井变体标记：true 时触发 LuckyWellTriggerEvent，抽一个非放井事件并在原位放信标
    private boolean wellVariant = false;

    public TheLastEndLuckyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THE_LAST_END_LUCKY_BLOCK.get(), pos, state);
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = luck;
    }

    public boolean isWellVariant() {
        return wellVariant;
    }

    public void setWellVariant(boolean wellVariant) {
        this.wellVariant = wellVariant;
    }

    //放置时从ItemStack的NBT读取数据
    public void readFromItemTag(CompoundTag tag) {
        if (tag.contains(TAG_LUCK)) {
            this.luck = tag.getInt(TAG_LUCK);
        }
        if (tag.contains(TAG_WELL_VARIANT)) {
            this.wellVariant = tag.getBoolean(TAG_WELL_VARIANT);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_LUCK, luck);
        if (wellVariant) tag.putBoolean(TAG_WELL_VARIANT, true);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.luck = tag.contains(TAG_LUCK) ? tag.getInt(TAG_LUCK) : 0;
        this.wellVariant = tag.getBoolean(TAG_WELL_VARIANT);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(LOOP);
            return software.bernie.geckolib.core.object.PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
