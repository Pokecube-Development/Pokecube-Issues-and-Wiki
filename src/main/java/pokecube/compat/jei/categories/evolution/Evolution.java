package pokecube.compat.jei.categories.evolution;

import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;

public class Evolution
{
    public Pokemob from;
    public Pokemob to;

    public EvolutionData data;

    public Evolution(final PokedexEntry evolver, final EvolutionData data)
    {
        this.from = Pokemob.ALLMAP.get(evolver);
        this.to = Pokemob.ALLMAP.get(data.evolution);
        this.data = data;
    }

}
