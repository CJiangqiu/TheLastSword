package net.the_last_sword.compat.projecte;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;
import net.the_last_sword.util.TheLastSwordLogger;
import net.the_last_sword.util.nbt.ItemLevelHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EMCMapper
public class DragonCrystalSmithingEmcMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    @Override
    public String getName() {
        return "DragonCrystalSmithing";
    }

    @Override
    public String getDescription() {
        return "Maps active dragon crystal smithing recipes with item levels.";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config,
                            ReloadableServerResources resources, RegistryAccess registries,
                            ResourceManager resourceManager) {
        // EMC重算可能早于配方同步事件，必须先应用当前配置的覆盖和禁用。
        ConfigRecipeManager.loadRecipes();
        List<DragonCrystalSmithingRecipe> recipes = ConfigRecipeManager.getAllRecipes();
        Map<Item, Set<Integer>> levels = new HashMap<>();
        for (DragonCrystalSmithingRecipe recipe : recipes) {
            ItemStack output = recipe.getResultItem(registries);
            levels.computeIfAbsent(output.getItem(), key -> new HashSet<>()).add(recipe.getOutputLevel());
            collectLevels(levels, recipe.getInput(), recipe.getInputLevel());
            if (recipe.hasTemplateInputLevel()) {
                collectLevels(levels, recipe.getTemplate(), recipe.getTemplateInputLevel());
            }
        }
        for (DragonCrystalSmithingRecipe recipe : recipes) {
            try {
                ItemStack output = recipe.getResultItem(registries);
                if (output.isEmpty()) {
                    continue;
                }
                setMappingLevel(output, recipe.getOutputLevel());
                NormalizedSimpleStack template = mapIngredient(mapper, recipe.getTemplate(),
                        recipe.hasTemplateInputLevel() ? recipe.getTemplateInputLevel() : null,
                        levels, recipe.getId() + "/template");
                NormalizedSimpleStack input = mapIngredient(mapper, recipe.getInput(), recipe.getInputLevel(),
                        levels, recipe.getId() + "/input");
                NormalizedSimpleStack addition = mapIngredient(mapper, recipe.getAddition(), null,
                        levels, recipe.getId() + "/addition");
                if (template != null && input != null && addition != null) {
                    // 锻造台直接消耗三个槽位，不返还桶等容器。
                    mapper.addConversion(output.getCount(), NSSItem.createItem(output), List.of(template, input, addition));
                }
            } catch (RuntimeException exception) {
                TheLastSwordLogger.info("Could not map smithing recipe {}: {}", recipe.getId(), exception.getMessage());
            }
        }
    }

    private static void collectLevels(Map<Item, Set<Integer>> levels, Ingredient ingredient, int level) {
        for (ItemStack stack : ingredient.getItems()) {
            if (!stack.isEmpty()) {
                levels.computeIfAbsent(stack.getItem(), key -> new HashSet<>()).add(level);
            }
        }
    }

    private static NormalizedSimpleStack mapIngredient(IMappingCollector<NormalizedSimpleStack, Long> mapper,
                                                       Ingredient ingredient, Integer level,
                                                       Map<Item, Set<Integer>> levels, String key) {
        Set<NormalizedSimpleStack> alternatives = new HashSet<>();
        for (ItemStack match : ingredient.getItems()) {
            if (match.isEmpty()) {
                continue;
            }
            if (level != null) {
                addAlternative(alternatives, ingredient, match, level);
            } else {
                addAlternative(alternatives, ingredient, match, ItemLevelHelper.getLevel(match));
                for (int knownLevel : levels.getOrDefault(match.getItem(), Set.of())) {
                    addAlternative(alternatives, ingredient, match, knownLevel);
                }
            }
        }
        if (alternatives.isEmpty()) {
            return null;
        }
        if (alternatives.size() == 1) {
            return alternatives.iterator().next();
        }
        // 使用中间节点避免三个槽位的候选材料产生笛卡尔积。
        NormalizedSimpleStack group = NSSFake.create("the_last_sword/smithing/" + key);
        for (NormalizedSimpleStack alternative : alternatives) {
            mapper.addConversion(1, group, Map.of(alternative, 1));
        }
        return group;
    }

    private static void addAlternative(Set<NormalizedSimpleStack> alternatives, Ingredient ingredient,
                                       ItemStack match, int level) {
        ItemStack stack = match.copy();
        ItemLevelHelper.setLevel(stack, level);
        if (ingredient.test(stack)) {
            setMappingLevel(stack, level);
            alternatives.add(NSSItem.createItem(stack));
        }
    }

    private static void setMappingLevel(ItemStack stack, int level) {
        // 缺省等级就是零，使普通合成产物和锻造产物共享基础价值。
        if (level == 0) {
            ItemLevelHelper.removeLevel(stack);
        } else {
            ItemLevelHelper.setLevel(stack, level);
        }
        // 新建耐久物品自动写入零损伤，不能让它成为等级价格键的一部分。
        if (stack.isDamageableItem() && stack.getDamageValue() == 0 && stack.hasTag()) {
            stack.getTag().remove("Damage");
        }
    }
}
