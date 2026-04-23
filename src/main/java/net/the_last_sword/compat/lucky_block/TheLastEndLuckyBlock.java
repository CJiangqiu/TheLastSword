package net.the_last_sword.compat.lucky_block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEvent;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventContext;
import net.the_last_sword.compat.lucky_block.lucky_event.LuckyEventRegistry;
import net.the_last_sword.compat.lucky_block.lucky_event.events.LuckyWellTriggerEvent;

import javax.annotation.Nullable;

//终焉幸运方块 - 行为参考原版LuckyBlock：红石/破坏/放置后被充能时触发幸运事件
public class TheLastEndLuckyBlock extends BaseEntityBlock {

    public TheLastEndLuckyBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .sound(SoundType.STONE)
            .strength(0.2F, 6_000_000.0F)
            .lightLevel(state -> 10)
            .noOcclusion()
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TheLastEndLuckyBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    //红石信号触发破坏
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean notify) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos, notify);
        if (world.hasNeighborSignal(pos)) {
            onBreak(this, world, null, pos, world.getBlockEntity(pos), ItemStack.EMPTY, true);
        }
    }

    //玩家破坏触发幸运事件
    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);
        //此时方块已被移除, BE 也已失效, 必须使用 Forge 在移除前捕获的 blockEntity 参数读 luck
        onBreak(this, world, player, pos, blockEntity, stack, false);
    }

    //放置时从ItemStack的NBT读luck到BE；若立刻被充能则当场触发破坏
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (world.getBlockEntity(pos) instanceof TheLastEndLuckyBlockEntity be) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                be.readFromItemTag(tag);
                be.setChanged();
            }
        }

        if (world.hasNeighborSignal(pos)) {
            onBreak(this, world, null, pos, world.getBlockEntity(pos), ItemStack.EMPTY, true);
        }
    }

    //统一破坏入口：精准采集掉落方块(保留luck)；否则触发幸运事件
    private static void onBreak(Block block, Level world, @Nullable Player player, BlockPos pos, @Nullable BlockEntity be, ItemStack tool, boolean removedByRedstone) {
        if (world.isClientSide) return;

        int luck = (be instanceof TheLastEndLuckyBlockEntity tle) ? tle.getLuck() : 0;
        boolean wellVariant = (be instanceof TheLastEndLuckyBlockEntity tle) && tle.isWellVariant();
        boolean silkTouch = !removedByRedstone && !tool.isEmpty()
            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        world.removeBlock(pos, false);
        world.removeBlockEntity(pos);

        if (silkTouch) {
            ItemStack drop = new ItemStack(block);
            if (luck != 0) drop.getOrCreateTag().putInt("Luck", luck);
            if (wellVariant) drop.getOrCreateTag().putBoolean("WellVariant", true);
            Block.popResource(world, pos, drop);
            return;
        }

        if (world instanceof ServerLevel serverLevel) {
            LuckyEventContext ctx = new LuckyEventContext(
                serverLevel, pos, player, luck, serverLevel.random, removedByRedstone
            );
            if (wellVariant) {
                new LuckyWellTriggerEvent().execute(ctx);
                return;
            }
            LuckyEvent event = LuckyEventRegistry.pickEvent(luck, serverLevel.random);
            if (event != null) {
                event.execute(ctx);
            }
        }
    }
}
