package pokecube.compat.jei.categories.evolution;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.compat.jei.ingredients.Pokemob;

public class Evolution
{
    public Pokemob from;
    public Pokemob to;

    public EvolutionData data;

    public Evolution(final PokedexEntry evolver, final EvolutionData data)
    {
        FormeHolder holder = evolver.getModel(IPokemob.MALE);
        this.from = holder == null ? this.from : Pokemob.FORMMAP.get(holder);
        if (this.from == null) this.from = Pokemob.ALLMAP.get(evolver);

        holder = data.evolution.getModel(IPokemob.MALE);
        if (data.data.getForme(data.evolution) != null) holder = data.data.getForme(data.evolution);
        this.to = holder == null ? this.to : Pokemob.FORMMAP.get(holder);
        if (this.to == null) this.to = Pokemob.ALLMAP.get(data.evolution);

        this.data = data;

        if (this.from == null || this.to == null)
            PokecubeAPI.LOGGER.error("Error with JEI Evolution for {} ({})", evolver, data.evolution);
    }

}
