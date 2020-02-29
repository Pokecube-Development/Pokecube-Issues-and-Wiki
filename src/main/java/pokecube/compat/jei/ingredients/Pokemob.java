package pokecube.compat.jei.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import pokecube.core.database.PokedexEntry;

public class Pokemob implements IIngredientType<PokedexEntry>
{
    @Override
    public Class<? extends PokedexEntry> getIngredientClass()
    {
        return PokedexEntry.class;
    }

}
