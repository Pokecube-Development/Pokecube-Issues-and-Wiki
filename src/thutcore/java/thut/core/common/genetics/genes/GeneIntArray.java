package thut.core.common.genetics.genes;

import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneIntArray implements Gene<int[]>
{
    protected int[] value = new int[0];

    @Override
    public int[] getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.value = tag.getIntArray("V");
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putIntArray("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final int[] value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + Arrays.toString(this.value);
    }

}
