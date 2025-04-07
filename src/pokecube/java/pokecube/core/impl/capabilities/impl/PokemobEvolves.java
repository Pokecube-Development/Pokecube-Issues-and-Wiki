package pokecube.core.impl.capabilities.impl;

import net.minecraft.world.item.ItemStack;

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
}
