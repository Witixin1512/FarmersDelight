package vectorwing.farmersdelight.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import org.openzen.zencode.java.ZenCodeType;

@FunctionalInterface
@ZenRegister
@ZenCodeType.Name("mods.farmersdelight.RecipeFunctionCuttingBoard")
@Document("mods/farmersdelight/RecipeFunctionCuttingBoard")
public interface RecipeFunctionCuttingBoard {

    @ZenCodeType.Method
    IItemStack process(IItemStack usualOut, IIngredient[] outputs, IItemStack tool);
}
