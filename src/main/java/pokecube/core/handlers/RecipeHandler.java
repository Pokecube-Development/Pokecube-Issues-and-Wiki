package pokecube.core.handlers;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import pokecube.core.items.berries.RecipeBrewBerries;

public class RecipeHandler
{
    public static void initRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        // event.getRegistry().register(RecipeRevive.SERIALIZER);
        // event.getRegistry().register(RecipePokeseals.SERIALIZER);
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
