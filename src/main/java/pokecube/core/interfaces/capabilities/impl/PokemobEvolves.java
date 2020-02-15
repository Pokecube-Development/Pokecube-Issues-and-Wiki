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
        this.dataSync().set(this.params.EVOLTICKDW, new Integer(evolutionTicks));
    }

    @Override
    public float getDynamaxFactor()
    {
        return this.dataSync().get(this.params.DYNAPOWERDW);
    }

    @Override
    public void setDynamaxFactor(float factor)
    {
        // Cap this so it is at least 1.
        factor = Math.max(1, factor);
        this.dataSync().set(this.params.DYNAPOWERDW, new Float(factor));
    }
}
