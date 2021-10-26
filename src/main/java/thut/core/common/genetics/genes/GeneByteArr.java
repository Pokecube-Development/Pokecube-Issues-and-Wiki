package thut.core.common.genetics.genes;

import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneByteArr implements Gene<byte[]>
{
    protected byte[] value = new byte[0];

    @Override
    public byte[] getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.value = tag.getByteArray("V");
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putByteArray("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final byte[] value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + Arrays.toString(this.value);
    }

}
