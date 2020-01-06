package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneString implements Gene
{
    protected String value = "";

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.value;
    }

    @Override
    public void load(CompoundNBT tag)
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
    public <T> void setValue(T value)
    {
        this.value = (String) value;
    }

}
