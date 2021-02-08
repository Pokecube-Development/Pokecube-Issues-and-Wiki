package pokecube.core.database.recipes;

import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;

public interface IRecipeParser
{
    default XMLRecipe deserialize(final String recipe)
    {
        return PokedexEntryLoader.gson.fromJson(recipe, XMLRecipe.class);
    }

    default String fileName(final String default_)
    {
        return default_;
    }

    /**
     * This is called before loading in recipes, to allow clearing old values,
     * etc
     */
    void init();

    void manageRecipe(XMLRecipe recipe) throws NullPointerException;

    default String serialize(final XMLRecipe recipe)
    {
        return PokedexEntryLoader.gson.toJson(recipe);
    }
}
