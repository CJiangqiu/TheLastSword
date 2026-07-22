package net.the_last_sword.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.the_last_sword.client.recipe.ClientDragonCrystalRecipeCache;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.recipe.DragonCrystalSmithingSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//服务端向客户端全量同步龙水晶锻造配方
public class SyncDragonCrystalRecipesPacket {

    private static final int MAX_RECIPE_COUNT = 4096;
    private static final DragonCrystalSmithingSerializer SERIALIZER = new DragonCrystalSmithingSerializer();

    private final List<DragonCrystalSmithingRecipe> recipes;

    public SyncDragonCrystalRecipesPacket(List<DragonCrystalSmithingRecipe> recipes) {
        this.recipes = List.copyOf(recipes);
    }

    public static void encode(SyncDragonCrystalRecipesPacket message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.recipes.size());
        for (DragonCrystalSmithingRecipe recipe : message.recipes) {
            buffer.writeResourceLocation(recipe.getId());
            SERIALIZER.toNetwork(buffer, recipe);
        }
    }

    public static SyncDragonCrystalRecipesPacket decode(FriendlyByteBuf buffer) {
        int recipeCount = buffer.readVarInt();
        if (recipeCount < 0 || recipeCount > MAX_RECIPE_COUNT) {
            throw new IllegalArgumentException("Invalid dragon crystal recipe count: " + recipeCount);
        }

        List<DragonCrystalSmithingRecipe> recipes = new ArrayList<>(recipeCount);
        for (int i = 0; i < recipeCount; i++) {
            ResourceLocation recipeId = buffer.readResourceLocation();
            recipes.add(SERIALIZER.fromNetwork(recipeId, buffer));
        }
        return new SyncDragonCrystalRecipesPacket(recipes);
    }

    public static void handle(SyncDragonCrystalRecipesPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientDragonCrystalRecipeCache.replaceAll(message.recipes));
        context.setPacketHandled(true);
    }
}
