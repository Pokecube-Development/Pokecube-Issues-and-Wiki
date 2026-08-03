package pokecube.core.impl.capabilities.impl;

import net.minecraft.world.item.ItemStack;
import pokecube.api.data.PokedexEntry;
import pokecube.core.utils.PokemobTracker;

public abstract class PokemobEvolves extends PokemobHungry
{

    @Override
    public ItemStack getEvolutionStack()
    {
        return this.stack;
    }

    /** @return the evolutionTicks */
    @Override
    public int getEvolutionTicks()
    {
        return this.params.EVOLTICKDW.get();
    }

    @Override
    public void setEvolutionStack(final ItemStack stack)
    {
        this.stack = stack;
    }

    /**
     * @param evolutionTicks
     *            the evolutionTicks to set
     */
    @Override
    public void setEvolutionTicks(final int evolutionTicks)
    {
        this.params.EVOLTICKDW.set(evolutionTicks);
    }

    PokedexEntry _evo_test;
    @Override
    public void preSyncClientSide()
    {
        super.preSyncClientSide();
        _evo_test = this.getPokedexEntry();
    }
    @Override
    public void postSyncClientSide()
    {
        super.postSyncClientSide();
        PokemobTracker.addPokemob(this);
    }
}
