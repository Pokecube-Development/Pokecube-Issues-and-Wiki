package pokecube.core.database.recipes;

import com.google.gson.JsonObject;

import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import thut.api.util.JsonUtil;

public interface IRecipeParser
{
    default XMLRecipe fromJson(final JsonObject recipe)
    {
        return JsonUtil.gson.fromJson(recipe, XMLRecipe.class);
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

    void manageRecipe(JsonObject recipe) throws NullPointerException;
}
