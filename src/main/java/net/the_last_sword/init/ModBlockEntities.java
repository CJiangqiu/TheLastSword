package net.the_last_sword.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.block.entity.DragonCrystalEnchantingTableBlockEntity;
import net.the_last_sword.block.entity.DragonCrystalSmithingTableBlockEntity;
import net.the_last_sword.compat.CompatCheck;
import net.the_last_sword.compat.lucky_block.TheLastEndLuckyBlockEntity;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<DragonCrystalSmithingTableBlockEntity>> DRAGON_CRYSTAL_SMITHING_TABLE =
        BLOCK_ENTITIES.register("dragon_crystal_smithing_table",
            () -> BlockEntityType.Builder.of(
                DragonCrystalSmithingTableBlockEntity::new,
                ModBlocks.DRAGON_CRYSTAL_SMITHING_TABLE.get()
            ).build(null)
        );

    public static final RegistryObject<BlockEntityType<DragonCrystalEnchantingTableBlockEntity>> DRAGON_CRYSTAL_ENCHANTING_TABLE =
        BLOCK_ENTITIES.register("dragon_crystal_enchanting_table",
            () -> BlockEntityType.Builder.of(
                DragonCrystalEnchantingTableBlockEntity::new,
                ModBlocks.DRAGON_CRYSTAL_ENCHANTING_TABLE.get()
            ).build(null)
        );

    //幸运方块联动（仅在 lucky 本体 mod 加载时注册）
    public static RegistryObject<BlockEntityType<TheLastEndLuckyBlockEntity>> THE_LAST_END_LUCKY_BLOCK;

    public static void register(IEventBus eventBus) {
        registerConditionalBlockEntities();
        BLOCK_ENTITIES.register(eventBus);
    }

    private static void registerConditionalBlockEntities() {
        if (CompatCheck.isLuckyBlockLoaded()) {
            THE_LAST_END_LUCKY_BLOCK = BLOCK_ENTITIES.register("the_last_end_lucky_block",
                () -> BlockEntityType.Builder.of(
                    TheLastEndLuckyBlockEntity::new,
                    ModBlocks.THE_LAST_END_LUCKY_BLOCK.get()
                ).build(null)
            );
        }
    }
}
