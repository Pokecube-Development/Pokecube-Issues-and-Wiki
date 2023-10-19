package pokecube.compat.jei.categories.evolution;

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
        this.from = Pokemob.ALLMAP.get(evolver);
        FormeHolder holder = evolver.getModel(IPokemob.MALE);
//        if (data.gender == 1) holder = evolver.getModel(IPokemob.MALE);
//        if (data.gender == 2) holder = evolver.getModel(IPokemob.FEMALE);
//        if (data.neededForme != null) holder = Database.formeHolders.get(data.neededForme);
        this.from = holder == null ? this.from : Pokemob.FORMMAP.get(holder);

        this.to = Pokemob.ALLMAP.get(data.evolution);
        holder = data.evolution.getModel(IPokemob.MALE);
//        if (data.gender == 1) holder = evolver.getModel(IPokemob.MALE);
//        if (data.gender == 2) holder = evolver.getModel(IPokemob.FEMALE);
        if (data.data.getForme(data.evolution) != null) holder = data.data.getForme(data.evolution);
        this.to = holder == null ? this.to : Pokemob.FORMMAP.get(holder);

        this.data = data;
    }

}
