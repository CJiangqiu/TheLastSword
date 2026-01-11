package net.the_last_sword.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;
import net.the_last_sword.init.ModBlockEntities;
import net.the_last_sword.init.ModItems;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;

public class DragonCrystalEnchantingTableBlockEntity extends RandomizableContainerBlockEntity implements GeoBlockEntity, WorldlyContainer {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    // 能量存储系统
    private final EnergyStorage energyStorage = new EnergyStorage(1073741824, 9126, 4096, 0);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    // 发电系统
    private static final int POWER_TIME_PER_CRYSTAL = 1800;
    private static final int ENERGY_PER_TICK = 10240;
    private int totalPowerTime = 0;

    // 水晶旋转动画
    private static final RawAnimation CRYSTAL_ROTATION = RawAnimation.begin().thenLoop("0");

    public DragonCrystalEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAGON_CRYSTAL_ENCHANTING_TABLE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(CRYSTAL_ROTATION);
            return null;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.the_last_sword.dragon_crystal_enchanting_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DragonCrystalEnchantingTableMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.the_last_sword.dragon_crystal_enchanting_table");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (!this.tryLoadLootTable(tag)) {
            this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, this.items);
        }
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        this.totalPowerTime = tag.getInt("TotalPowerTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.put("Energy", energyStorage.serializeNBT());
        tag.putInt("TotalPowerTime", this.totalPowerTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove) {
            if (facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
                return handlers[facing.ordinal()].cast();
            if (capability == ForgeCapabilities.ENERGY)
                return energyHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
        energyHandler.invalidate();
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public int getTotalPowerTime() {
        return totalPowerTime;
    }

    // 提取能量（用于附魔消耗）
    public int extractEnergy(int amount) {
        int extracted = energyStorage.extractEnergy(amount, false);
        if (extracted > 0) {
            setChanged();
        }
        return extracted;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DragonCrystalEnchantingTableBlockEntity blockEntity) {
        if (level.isClientSide)
            return;

        boolean changed = false;

        // 检测并消耗龙水晶
        ItemStack fuelSlot = blockEntity.getItem(0);
        if (!fuelSlot.isEmpty() && fuelSlot.is(ModItems.DRAGON_CRYSTAL.get())) {
            fuelSlot.shrink(1);
            blockEntity.totalPowerTime += POWER_TIME_PER_CRYSTAL;
            changed = true;
        }

        // 发电逻辑
        if (blockEntity.totalPowerTime > 0) {
            blockEntity.totalPowerTime--;
            changed = true;

            int remainingEnergy = ENERGY_PER_TICK;

            // 优先给绿色槽位物品充能
            ItemStack chargeSlot = blockEntity.getItem(1);
            if (!chargeSlot.isEmpty()) {
                var energyCap = chargeSlot.getCapability(ForgeCapabilities.ENERGY);
                if (energyCap.isPresent()) {
                    var itemEnergy = energyCap.orElse(null);
                    if (itemEnergy != null) {
                        int canReceive = itemEnergy.getMaxEnergyStored() - itemEnergy.getEnergyStored();
                        int toCharge = Math.min(remainingEnergy, canReceive);
                        if (toCharge > 0) {
                            itemEnergy.receiveEnergy(toCharge, false);
                            remainingEnergy -= toCharge;
                        }
                    }
                }
            }

            // 剩余能量存储到方块自身
            if (remainingEnergy > 0) {
                blockEntity.energyStorage.receiveEnergy(remainingEnergy, false);
            }
        }

        // 能量传输逻辑
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE != null) {
                neighborBE.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(neighborEnergy -> {
                    // 尝试从邻居接收能量
                    if (blockEntity.energyStorage.getEnergyStored() < blockEntity.energyStorage.getMaxEnergyStored()) {
                        int received = neighborEnergy.extractEnergy(9126, true);
                        if (received > 0) {
                            int actualReceived = blockEntity.energyStorage.receiveEnergy(received, false);
                            neighborEnergy.extractEnergy(actualReceived, false);
                            blockEntity.setChanged();
                        }
                    }
                    // 尝试向邻居传输能量
                    if (blockEntity.energyStorage.getEnergyStored() > 0) {
                        int sent = blockEntity.energyStorage.extractEnergy(4096, true);
                        if (sent > 0) {
                            int actualSent = neighborEnergy.receiveEnergy(sent, false);
                            blockEntity.energyStorage.extractEnergy(actualSent, false);
                            blockEntity.setChanged();
                        }
                    }
                });
            }
        }

        if (changed) {
            blockEntity.setChanged();
        }

    }
}
