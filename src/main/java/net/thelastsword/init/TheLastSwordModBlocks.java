package net.thelastsword.init;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thelastsword.TheLastSwordMod;
import net.thelastsword.block.DragonCrystalSmithingTableBlock;

public class TheLastSwordModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, TheLastSwordMod.MODID);
	public static final RegistryObject<Block> DRAGON_CRYSTAL_SMITHING_TABLE = REGISTRY.register("dragon_crystal_smithing_table", () -> new DragonCrystalSmithingTableBlock());

}
