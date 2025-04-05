package thut.core.common.genetics.genes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneBoolean implements Gene<Boolean>
{
    protected Boolean value = Boolean.FALSE;

    @Override
    public Boolean getValue()
    {
        return this.value;
    }

    @Override
    public void load(Provider provider, final CompoundTag tag)
    {
        this.value = tag.getBoolean("V");
    }

    @Override
    public CompoundTag save(Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final Boolean value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + this.value;
    }

}
