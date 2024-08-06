package net.thelastsword.init;

import net.thelastsword.block.entity.DragonCrystalSmithingTableBlockEntity;
import net.thelastsword.TheLastSwordMod;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

public class TheLastSwordModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TheLastSwordMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> DRAGON_CRYSTAL_SMITHING_TABLE = register("dragon_crystal_smithing_table", TheLastSwordModBlocks.DRAGON_CRYSTAL_SMITHING_TABLE, DragonCrystalSmithingTableBlockEntity::new);

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
