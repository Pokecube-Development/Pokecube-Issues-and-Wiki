package pokecube.core.interfaces.capabilities.impl;

import net.minecraft.item.ItemStack;

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
        return this.dataSync().get(this.params.EVOLTICKDW);
    }

    @Override
    public void setEvolutionStack(ItemStack stack)
    {
        this.stack = stack;
    }

    /**
     * @param evolutionTicks
     *            the evolutionTicks to set
     */
    @Override
    public void setEvolutionTicks(int evolutionTicks)
    {
        this.dataSync().set(this.params.EVOLTICKDW, new Integer(evolutionTicks));
    }
}
