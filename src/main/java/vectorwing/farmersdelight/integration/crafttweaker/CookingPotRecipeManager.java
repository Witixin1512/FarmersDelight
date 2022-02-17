package vectorwing.farmersdelight.integration.crafttweaker;

import com.blamejared.crafttweaker.CraftTweaker;
import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.managers.IRecipeManager;
import com.blamejared.crafttweaker.api.recipes.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipes.IReplacementRule;
import com.blamejared.crafttweaker.api.recipes.ReplacementHandlerHelper;
import com.blamejared.crafttweaker.api.util.StringUtils;
import com.blamejared.crafttweaker.impl.actions.recipes.ActionAddRecipe;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;
import vectorwing.farmersdelight.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.utils.ListUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Farmer's Delight Cooking Pot recipes.
 *
 * @docParam this <recipetype:farmersdelight:cooking>
 */
@IRecipeHandler.For(CookingPotRecipe.class)
@Document("mods/farmersdelight/CookingPot")
@ZenRegister
@ZenCodeType.Name("mods.farmersdelight.CookingPot")
public class CookingPotRecipeManager implements IRecipeManager, IRecipeHandler<CookingPotRecipe>
{
    /**
     * Add a cooking pot recipe.
     *
     * @param name       Name of the recipe to add
     * @param output     Output item
     * @param inputs     Input ingredients
     * @param container  Container item
     * @param experience Experience granted
     * @param cookTime   Cooking time
     *
     * @docParam name "cooking_pot_test"
     * @docParam output <item:minecraft:enchanted_golden_apple>
     * @docParam inputs [<item:minecraft:gold_block>]
     * @docParam container <item:minecraft:apple>
     * @docParam experience 100
     * @docParam cookTime 400
     */
    @ZenCodeType.Method
    public void addRecipe(String name,
                          IItemStack output,
                          IIngredient[] inputs,
                          @ZenCodeType.Optional IItemStack container,
                          @ZenCodeType.OptionalFloat float experience,
                          @ZenCodeType.OptionalInt(200) int cookTime, @ZenCodeType.Optional RecipeFunctionCookingPot functionArray) {
        if (!validateInputs(inputs)) return;
        CookingPotRecipe recipe;
        if (functionArray == null){
            recipe = new CookingPotRecipe(new ResourceLocation(CraftTweaker.MODID, name),
                    "",
                    ListUtils.mapArrayIndexSet(inputs,
                            IIngredient::asVanillaIngredient,
                            NonNullList.withSize(inputs.length, Ingredient.EMPTY)),
                    output.getInternal(),
                    container == null ? ItemStack.EMPTY : container.getInternal(),
                    experience,
                    cookTime);
        }
        else {
            recipe = new CookingPotWithFunction(new ResourceLocation(CraftTweaker.MODID, name),
                    "",
                    ListUtils.mapArrayIndexSet(inputs,
                            IIngredient::asVanillaIngredient,
                            NonNullList.withSize(inputs.length, Ingredient.EMPTY)),
                    output.getInternal(),
                    container == null ? ItemStack.EMPTY : container.getInternal(),
                    experience,
                    cookTime, functionArray);
        }
        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                recipe,
                ""));
    }

    private boolean validateInputs(IIngredient[] inputs) {
        if (inputs.length == 0) {
            CraftTweakerAPI.logError("No ingredients for cooking recipe");
            return false;
        } else if (inputs.length > CookingPotRecipe.INPUT_SLOTS) {
            CraftTweakerAPI.logError("Too many ingredients for cooking recipe! The max is %s", CookingPotRecipe.INPUT_SLOTS);
            return false;
        }
        return true;
    }

    @Override
    public String dumpToCommandString(IRecipeManager manager, CookingPotRecipe recipe) {
        return String.format(
                "%s.addRecipe(%s, %s, %s, %s, %s, %s);",
                manager.getCommandString(),
                StringUtils.quoteAndEscape(recipe.getId()),
                new MCItemStackMutable(recipe.getResultItem()).getCommandString(),
                recipe.getIngredients().stream()
                        .map(IIngredient::fromIngredient)
                        .map(IIngredient::getCommandString)
                        .collect(Collectors.joining(", ", "[", "]")),
                new MCItemStackMutable(recipe.getOutputContainer()).getCommandString(),
                recipe.getExperience(),
                recipe.getCookTime()
        );
    }

    @Override
    public Optional<Function<ResourceLocation, CookingPotRecipe>> replaceIngredients(IRecipeManager manager, CookingPotRecipe recipe, List<IReplacementRule> rules) {
        return ReplacementHandlerHelper.replaceNonNullIngredientList(
                recipe.getIngredients(),
                Ingredient.class,
                recipe,
                rules, newIngredients -> {
            if (recipe instanceof CookingPotRecipeManager.CookingPotWithFunction){
                    return id -> new CookingPotWithFunction(id, recipe.getGroup(), recipe.getIngredients(), recipe.getResultItem(), recipe.getOutputContainer(), recipe.getExperience(), recipe.getCookTime(), ((CookingPotWithFunction) recipe).getFunction());
            }
            return id -> new CookingPotRecipe(id, recipe.getGroup(), recipe.getIngredients(), recipe.getResultItem(), recipe.getOutputContainer(), recipe.getExperience(), recipe.getCookTime());
        });
    }

    private static class CookingPotWithFunction extends CookingPotRecipe {
        private final RecipeFunctionCookingPot function;

        public CookingPotWithFunction(ResourceLocation resourceLocation, String group, NonNullList<Ingredient> inputList, ItemStack output, ItemStack container, float experience, int cookTime, RecipeFunctionCookingPot function) {
            super(resourceLocation, group, inputList, output, container, experience, cookTime);
            this.function = function;
        }

        @Nonnull
        @Override
        public ItemStack getResultItem() {
            return function.process(new MCItemStack(super.getResultItem()), convertIngredients(getIngredients()), new MCItemStack(getOutputContainer())).getInternal().copy();
        }

        private RecipeFunctionCookingPot getFunction() {
            return function;
        }

        private IItemStack[] convertIngredients(NonNullList<Ingredient> ingList){
           List<ItemStack> stackList = new ArrayList<>();
           ingList.stream().forEach(ingr -> stackList.addAll(Arrays.asList(ingr.getItems())));
           return stackList.stream().map(MCItemStack::new).toArray(MCItemStack[]::new);
        }
    }

    @Override
    public IRecipeType<CookingPotRecipe> getRecipeType() {
        return CookingPotRecipe.TYPE;
    }
}
