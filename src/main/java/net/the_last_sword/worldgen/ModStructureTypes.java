package net.the_last_sword.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;

public final class ModStructureTypes {
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<StructureType<TravelersHoldStructure>> TRAVELERS_HOLD =
            STRUCTURE_TYPES.register("travelers_hold", () -> () -> TravelersHoldStructure.CODEC);

    private ModStructureTypes() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
