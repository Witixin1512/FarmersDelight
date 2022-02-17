package vectorwing.farmersdelight.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.farmersdelight.RecipeFunctionCookingPot")
@Document("mods/farmersdelight/RecipeFunctionCookingPot")
public interface RecipeFunctionCookingPot {

    @ZenCodeType.Method
    IItemStack process(IItemStack usualOut, IItemStack[] ingredients, IItemStack tool);
}

