package net.the_last_sword.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.the_last_sword.TheLastSwordMod;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.recipe.DragonCrystalSmithingSerializer;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TheLastSwordMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
        DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, TheLastSwordMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<DragonCrystalSmithingRecipe>> DRAGON_CRYSTAL_SMITHING_SERIALIZER =
        SERIALIZERS.register("dragon_crystal_smithing", DragonCrystalSmithingSerializer::new);

    public static final RegistryObject<RecipeType<DragonCrystalSmithingRecipe>> DRAGON_CRYSTAL_SMITHING_TYPE =
        TYPES.register("dragon_crystal_smithing", () -> new RecipeType<DragonCrystalSmithingRecipe>() {
            @Override
            public String toString() {
                return "dragon_crystal_smithing";
            }
        });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
