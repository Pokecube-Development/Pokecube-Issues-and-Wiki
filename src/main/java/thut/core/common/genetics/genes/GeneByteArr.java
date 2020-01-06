package thut.core.common.genetics.genes;

import java.util.Arrays;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneByteArr implements Gene
{
    protected byte[] value = new byte[0];

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) this.value;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        this.value = tag.getByteArray("V");
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putByteArray("V", this.value);
        return tag;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (byte[]) value;
    }

    @Override
    public String toString()
    {
        return "" + Arrays.toString(this.value);
    }

}
