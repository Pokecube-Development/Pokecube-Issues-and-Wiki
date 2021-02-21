package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneString implements Gene<String>
{
    protected String value = "";

    @Override
    public String getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundNBT tag)
    {
        this.value = tag.getString("V");
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final String value)
    {
        this.value = value;
    }

}
