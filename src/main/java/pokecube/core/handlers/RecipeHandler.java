package pokecube.core.handlers;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import pokecube.core.items.berries.RecipeBrewBerries;
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.items.revive.RecipeRevive;

public class RecipeHandler
{
    public static void initRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().register(RecipeRevive.SERIALIZER.setRegistryName(new ResourceLocation("pokecube:revive")));
        event.getRegistry().register(RecipePokeseals.SERIALIZER.setRegistryName(new ResourceLocation(
                "pokecube:seal_apply")));
        BrewingRecipeRegistry.addRecipe(new RecipeBrewBerries());
    }
}
