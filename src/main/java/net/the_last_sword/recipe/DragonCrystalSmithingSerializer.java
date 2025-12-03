package net.the_last_sword.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class DragonCrystalSmithingSerializer implements RecipeSerializer<DragonCrystalSmithingRecipe> {

    @Override
    public DragonCrystalSmithingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient template = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "template"));

        JsonObject inputJson = GsonHelper.getAsJsonObject(json, "input");
        Ingredient input = Ingredient.fromJson(inputJson);
        int inputLevel = GsonHelper.getAsInt(inputJson, "inputLevel", 0);

        Ingredient addition = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "addition"));

        JsonObject outputJson = GsonHelper.getAsJsonObject(json, "output");
        ItemStack output = ShapedRecipe.itemStackFromJson(outputJson);
        int outputLevel = GsonHelper.getAsInt(outputJson, "outputLevel", 0);

        return new DragonCrystalSmithingRecipe(recipeId, template, input, inputLevel, addition, output, outputLevel);
    }

    @Override
    public DragonCrystalSmithingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient template = Ingredient.fromNetwork(buffer);
        Ingredient input = Ingredient.fromNetwork(buffer);
        int inputLevel = buffer.readInt();
        Ingredient addition = Ingredient.fromNetwork(buffer);
        ItemStack output = buffer.readItem();
        int outputLevel = buffer.readInt();

        return new DragonCrystalSmithingRecipe(recipeId, template, input, inputLevel, addition, output, outputLevel);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, DragonCrystalSmithingRecipe recipe) {
        recipe.getTemplate().toNetwork(buffer);
        recipe.getInput().toNetwork(buffer);
        buffer.writeInt(recipe.getInputLevel());
        recipe.getAddition().toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem(null));
        buffer.writeInt(recipe.getOutputLevel());
    }
}
