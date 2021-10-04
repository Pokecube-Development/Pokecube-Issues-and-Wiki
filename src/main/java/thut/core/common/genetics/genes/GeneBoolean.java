package thut.core.common.genetics.genes;

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
    public void load(final CompoundTag tag)
    {
        this.value = tag.getBoolean("V");
    }

    @Override
    public CompoundTag save()
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
