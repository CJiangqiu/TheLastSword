package net.the_last_sword.client.recipe;

import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

//客户端龙水晶锻造配方缓存，内容只接受服务端同步，禁止读取本地config冒充服务端配置
public final class ClientDragonCrystalRecipeCache {

    private static final Map<ResourceLocation, DragonCrystalSmithingRecipe> RECIPES = new LinkedHashMap<>();
    private static Consumer<List<DragonCrystalSmithingRecipe>> recipeViewerListener = recipes -> { };

    private ClientDragonCrystalRecipeCache() {
    }

    public static synchronized void replaceAll(List<DragonCrystalSmithingRecipe> recipes) {
        RECIPES.clear();
        for (DragonCrystalSmithingRecipe recipe : recipes) {
            RECIPES.put(recipe.getId(), recipe);
        }
        notifyRecipeViewers();
    }

    public static synchronized void clear() {
        RECIPES.clear();
        notifyRecipeViewers();
    }

    public static synchronized List<DragonCrystalSmithingRecipe> getAllRecipes() {
        return Collections.unmodifiableList(new ArrayList<>(RECIPES.values()));
    }

    public static synchronized Optional<DragonCrystalSmithingRecipe> getRecipe(ResourceLocation id) {
        DragonCrystalSmithingRecipe exactRecipe = RECIPES.get(id);
        if (exactRecipe != null) {
            return Optional.of(exactRecipe);
        }

        // 配方允许放进 config 下的任意子目录，目录名会成为实际 ID 的一部分。
        // 卷轴引用稳定的文件名，因此精确匹配失败时按命名空间和文件名回退查找。
        String requestedFileName = recipeFileName(id);
        DragonCrystalSmithingRecipe matchedRecipe = null;
        for (Map.Entry<ResourceLocation, DragonCrystalSmithingRecipe> entry : RECIPES.entrySet()) {
            ResourceLocation candidateId = entry.getKey();
            if (!candidateId.getNamespace().equals(id.getNamespace())
                    || !recipeFileName(candidateId).equals(requestedFileName)) {
                continue;
            }

            // 同名配方存在歧义时不随意选择，避免卷轴展示错误内容。
            if (matchedRecipe != null) {
                return Optional.empty();
            }
            matchedRecipe = entry.getValue();
        }
        return Optional.ofNullable(matchedRecipe);
    }

    private static String recipeFileName(ResourceLocation id) {
        String path = id.getPath();
        int separator = path.lastIndexOf('/');
        return separator >= 0 ? path.substring(separator + 1) : path;
    }

    public static synchronized void setRecipeViewerListener(
            Consumer<List<DragonCrystalSmithingRecipe>> listener) {
        recipeViewerListener = listener;
        notifyRecipeViewers();
    }

    public static synchronized void clearRecipeViewerListener() {
        recipeViewerListener = recipes -> { };
    }

    private static void notifyRecipeViewers() {
        recipeViewerListener.accept(new ArrayList<>(RECIPES.values()));
    }
}
