package net.the_last_sword.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.the_last_sword.init.ModRecipes;
import net.the_last_sword.util.nbt.ItemLevelHelper;
import net.the_last_sword.util.TheLastSwordLogger;

public class DragonCrystalSmithingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient template;
    private final Ingredient input;
    private final int inputLevel;
    private final Ingredient addition;
    private final ItemStack output;
    private final int outputLevel;

    public DragonCrystalSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient input, int inputLevel, Ingredient addition, ItemStack output, int outputLevel) {
        this.id = id;
        this.template = template;
        this.input = input;
        this.inputLevel = inputLevel;
        this.addition = addition;
        this.output = output;
        this.outputLevel = outputLevel;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 3) {
            return false;
        }

        ItemStack templateStack = container.getItem(0);
        ItemStack baseStack = container.getItem(1);
        ItemStack additionStack = container.getItem(2);

        boolean templateMatch = template.test(templateStack);
        boolean inputMatch = input.test(baseStack);
        boolean additionMatch = addition.test(additionStack);

        TheLastSwordLogger.debug("Recipe {} - Template: {}, Input: {}, Addition: {}",
            id, templateMatch, inputMatch, additionMatch);

        if (!templateMatch || !inputMatch || !additionMatch) {
            return false;
        }

        //检查输入物品的等级是否匹配 inputLevel
        int baseItemLevel = ItemLevelHelper.getLevel(baseStack);
        boolean levelMatch = baseItemLevel == inputLevel;

        TheLastSwordLogger.debug("Recipe {} - Required level: {}, Item level: {}, Match: {}",
            id, inputLevel, baseItemLevel, levelMatch);

        return levelMatch;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        ItemStack outputStack = output.copy();
        ItemStack inputStack = container.getItem(1);

        //复制附魔
        EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(inputStack), outputStack);

        //复制纹饰（Trim）
        if (inputStack.hasTag() && inputStack.getTag().contains("Trim")) {
            outputStack.getOrCreateTag().put("Trim", inputStack.getTag().get("Trim").copy());
        }

        //复制自定义名称
        if (inputStack.hasCustomHoverName()) {
            outputStack.setHoverName(inputStack.getHoverName());
        }

        //设置输出物品的等级（所有属性都会根据等级动态计算）
        ItemLevelHelper.setLevel(outputStack, outputLevel);

        return outputStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DRAGON_CRYSTAL_SMITHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DRAGON_CRYSTAL_SMITHING_TYPE.get();
    }

    //为JEI和序列化器提供getter方法
    public Ingredient getTemplate() {
        return template;
    }

    public Ingredient getInput() {
        return input;
    }

    public int getInputLevel() {
        return inputLevel;
    }

    public Ingredient getAddition() {
        return addition;
    }

    public int getOutputLevel() {
        return outputLevel;
    }
}
