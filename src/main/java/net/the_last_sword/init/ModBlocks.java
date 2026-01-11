package net.the_last_sword.init;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.block.DragonCrystalEnchantingTableBlock;
import net.the_last_sword.block.DragonCrystalSmithingTableBlock;
import net.the_last_sword.block.DragonSoulLanternBlock;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<Block> DRAGON_CRYSTAL_SMITHING_TABLE = BLOCKS.register("dragon_crystal_smithing_table",
        DragonCrystalSmithingTableBlock::new
    );

    public static final RegistryObject<Block> DRAGON_SOUL_LANTERN = BLOCKS.register("dragon_soul_lantern",
        DragonSoulLanternBlock::new
    );

    public static final RegistryObject<Block> DRAGON_CRYSTAL_ENCHANTING_TABLE = BLOCKS.register("dragon_crystal_enchanting_table",
        DragonCrystalEnchantingTableBlock::new
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
