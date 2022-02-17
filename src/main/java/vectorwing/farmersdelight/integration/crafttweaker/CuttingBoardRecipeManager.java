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
import vectorwing.farmersdelight.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.crafting.ingredients.ChanceResult;
import vectorwing.farmersdelight.integration.crafttweaker.actions.ActionRemoveCuttingBoardRecipe;
import vectorwing.farmersdelight.utils.ListUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Farmer's Delight Cutting Board recipes.
 *
 * @docParam this <recipetype:farmersdelight:cutting>
 */
@IRecipeHandler.For(CuttingBoardRecipe.class)
@Document("mods/farmersdelight/CuttingBoard")
@ZenRegister
@ZenCodeType.Name("mods.farmersdelight.CuttingBoard")
public class CuttingBoardRecipeManager implements IRecipeManager, IRecipeHandler<CuttingBoardRecipe>
{
    /**
     * Add a cutting board recipe.
     *
     * @param name    Name of the recipe to add
     * @param input   Input ingredient
     * @param results Output items
     * @param tool    Tool ingredient
     * @param sound   Sound event name
     *
     * @docParam name "cutting_board_test"
     * @docParam input <item:minecraft:gravel>
     * @docParam results [<item:minecraft:flint>]
     * @docParam tool <item:minecraft:string>
     * @docParam sound "minecraft:block.gravel.break"
     */
    @ZenCodeType.Method
    public void addRecipe(String name,
                          IIngredient input,
                          IItemStack[] results,
                          IIngredient tool,
                          @ZenCodeType.OptionalString String sound, @ZenCodeType.Optional RecipeFunctionCuttingBoard function ) {

        CuttingBoardRecipe recipe;
        if (function == null){
            recipe = new CuttingBoardRecipe(new ResourceLocation(CraftTweaker.MODID, name),
                    "",
                    input.asVanillaIngredient(),
                    tool.asVanillaIngredient(),
                    ListUtils.mapArrayIndexSet(results,
                            (stack) -> new ChanceResult(stack.getInternal(), 1),
                            NonNullList.withSize(results.length, ChanceResult.EMPTY)),
                    sound);
        }
        else {
            recipe = new CuttingBoardWithFunction(new ResourceLocation(CraftTweaker.MODID, name),
                    "",
                    input.asVanillaIngredient(),
                    tool.asVanillaIngredient(),
                    ListUtils.mapArrayIndexSet(results,
                            (stack) -> new ChanceResult(stack.getInternal(), 1),
                            NonNullList.withSize(results.length, ChanceResult.EMPTY)),
                    sound, function);
        }

        CraftTweakerAPI.apply(new ActionAddRecipe(this,
                recipe, ""));
    }

    @Override
    public void removeRecipe(IItemStack output) {
        removeRecipe(new IItemStack[]{output});
    }

    /**
     * Remove a cutting board recipe with multiple outputs.
     *
     * @param outputs Output items
     *
     * @docParam outputs [<item:farmersdelight:cooked_salmon_slice> * 2, <item:minecraft:bone_meal>]
     */
    @ZenCodeType.Method
    public void removeRecipe(IItemStack[] outputs) {
        CraftTweakerAPI.apply(new ActionRemoveCuttingBoardRecipe(this, outputs));
    }

    @Override
    public IRecipeType<CuttingBoardRecipe> getRecipeType() {
        return CuttingBoardRecipe.TYPE;
    }

    @Override
    public String dumpToCommandString(IRecipeManager manager, CuttingBoardRecipe recipe) {
        return String.format(
                "%s.addRecipe(%s, %s, %s, %s, %s);",
                manager.getCommandString(),
                StringUtils.quoteAndEscape(recipe.getId()),
                IIngredient.fromIngredient(recipe.getIngredients().get(0)).getCommandString(),
                recipe.getResults().stream()
                        .map(MCItemStackMutable::new)
                        .map(MCItemStackMutable::getCommandString)
                        .collect(Collectors.joining(", ", "[", "]")),
                IIngredient.fromIngredient(recipe.getTool()).getCommandString(),
                recipe.getSoundEventID()
        );
    }

    @Override
    public Optional<Function<ResourceLocation, CuttingBoardRecipe>> replaceIngredients(IRecipeManager manager, CuttingBoardRecipe recipe, List<IReplacementRule> rules) {
        return ReplacementHandlerHelper.replaceIngredientList(
                recipe.getIngredientsAndTool(),
                Ingredient.class,
                recipe,
                rules,
                newIngredients -> {
                    if (recipe instanceof CuttingBoardRecipeManager.CuttingBoardWithFunction){
                        return id -> new CuttingBoardRecipeManager.CuttingBoardWithFunction(id, recipe.getGroup(), recipe.getIngredients().get(0), recipe.getTool(), recipe.getRollableResults(), recipe.getSoundEventID() , ((CuttingBoardRecipeManager.CuttingBoardWithFunction) recipe).getFunction());
                    }
                    return id -> new CuttingBoardRecipe(id, recipe.getGroup(), recipe.getIngredients().get(0), recipe.getTool(), recipe.getRollableResults(), recipe.getSoundEventID() );
                });
    }

    private static class CuttingBoardWithFunction extends CuttingBoardRecipe {
        private final RecipeFunctionCuttingBoard function;

        public CuttingBoardWithFunction(ResourceLocation resourceLocation, String group, Ingredient tool, Ingredient input,  NonNullList<ChanceResult> results, String soundEvent, RecipeFunctionCuttingBoard function) {
            super(resourceLocation, group, input, tool, results, soundEvent);
            this.function = function;
        }

        @Nonnull
        @Override
        public ItemStack getResultItem() {
            return function.process(new MCItemStack(super.getResultItem()), getIngredients().stream().map(ingr -> IIngredient.fromIngredient(ingr)).toArray(IIngredient[]::new), new MCItemStack(getTool().getItems()[0])).getInternal().copy();
        }

        private RecipeFunctionCuttingBoard getFunction() {
            return function;
        }
    }

}
