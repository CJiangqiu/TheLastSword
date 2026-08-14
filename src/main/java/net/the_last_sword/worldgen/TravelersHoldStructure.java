package net.the_last_sword.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.the_last_sword.TheLastSwordMod;

import java.util.Optional;

/** 在下界洞穴的真实地表上生成旅行者据点，避免普通高度图命中基岩天花板。 */
public class TravelersHoldStructure extends Structure {
    public static final Codec<TravelersHoldStructure> CODEC = simpleCodec(TravelersHoldStructure::new);

    private static final ResourceKey<StructureTemplatePool> START_POOL = ResourceKey.create(
            Registries.TEMPLATE_POOL,
            new ResourceLocation(TheLastSwordMod.MOD_ID, "travelers_hold"));
    private static final int MAX_SEARCH_Y = 112;
    private static final int MIN_SEARCH_Y = 32;
    // 模板本身高 26 格；只有整座据点上方空间足够时才接受这个地表。
    private static final int REQUIRED_CLEARANCE = 26;
    private static final int MAX_DISTANCE_FROM_CENTER = 64;

    public TravelersHoldStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        NoiseColumn column = context.chunkGenerator().getBaseColumn(
                x, z, context.heightAccessor(), context.randomState());

        int floorY = findCavernFloor(column, x, z);
        if (floorY < MIN_SEARCH_Y) {
            return Optional.empty();
        }

        Registry<StructureTemplatePool> pools = context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> startPool = pools.getHolderOrThrow(START_POOL);
        BlockPos startPos = new BlockPos(x, floorY + 1, z);
        return JigsawPlacement.addPieces(
                context,
                startPool,
                Optional.empty(),
                1,
                startPos,
                false,
                Optional.empty(),
                MAX_DISTANCE_FROM_CENTER);
    }

    private static int findCavernFloor(NoiseColumn column, int x, int z) {
        BlockPos.MutableBlockPos supportPos = new BlockPos.MutableBlockPos(x, MAX_SEARCH_Y, z);
        for (int y = MAX_SEARCH_Y; y >= MIN_SEARCH_Y; y--) {
            BlockState floor = column.getBlock(y);
            if (!floor.isFaceSturdy(EmptyBlockGetter.INSTANCE, supportPos.setY(y), Direction.UP)) {
                continue;
            }

            boolean clear = true;
            for (int offset = 1; offset <= REQUIRED_CLEARANCE; offset++) {
                if (!column.getBlock(y + offset).isAir()) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.TRAVELERS_HOLD.get();
    }
}
