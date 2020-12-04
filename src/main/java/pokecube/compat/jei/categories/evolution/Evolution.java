package pokecube.compat.jei.categories.evolution;

import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.interfaces.IPokemob.FormeHolder;

public class Evolution
{
    public Pokemob from;
    public Pokemob to;

    public EvolutionData data;

    public Evolution(final PokedexEntry evolver, final EvolutionData data)
    {
        this.from = Pokemob.ALLMAP.get(evolver);
        FormeHolder holder = null;
        if (data.neededForme != null) holder = Database.formeHolders.get(data.neededForme);
        this.to = holder == null ? Pokemob.ALLMAP.get(data.evolution) : Pokemob.FORMMAP.get(holder);
        this.data = data;
    }

}
