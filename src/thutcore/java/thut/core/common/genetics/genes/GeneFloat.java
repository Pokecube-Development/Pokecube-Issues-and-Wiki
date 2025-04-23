package thut.core.common.genetics.genes;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneFloat implements Gene<Float>
{
    protected Float value = Float.valueOf(0);

    @Override
    public Float getValue()
    {
        return this.value;
    }

    @Override
    public void load(Provider provider, final CompoundTag tag)
    {
        this.value = tag.getFloat("V");
        if (this.value.isNaN()) this.value = 0f;
    }

    @Override
    public CompoundTag save(Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        if (this.value.isNaN()) this.value = 0f;
        tag.putFloat("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final Float value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + this.value;
    }

}
