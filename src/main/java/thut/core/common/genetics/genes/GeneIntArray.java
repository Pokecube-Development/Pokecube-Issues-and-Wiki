package thut.core.common.genetics.genes;

import java.util.Arrays;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneIntArray implements Gene
{
    protected int[] value = new int[0];

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.value;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        this.value = tag.getIntArray("V");
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putIntArray("V", this.value);
        return tag;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (int[]) value;
    }

    @Override
    public String toString()
    {
        return "" + Arrays.toString(this.value);
    }

}
