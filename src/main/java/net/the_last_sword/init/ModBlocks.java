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
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.lucky_block.TheLastEndLuckyBlock;

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

    //幸运方块联动（仅在 lucky 本体 mod 加载时注册）
    public static RegistryObject<Block> THE_LAST_END_LUCKY_BLOCK;

    public static void register(IEventBus eventBus) {
        registerConditionalBlocks();
        BLOCKS.register(eventBus);
    }

    private static void registerConditionalBlocks() {
        if (CompatCheck.isLuckyBlockLoaded()) {
            THE_LAST_END_LUCKY_BLOCK = BLOCKS.register("the_last_end_lucky_block", TheLastEndLuckyBlock::new);
        }
    }
}
