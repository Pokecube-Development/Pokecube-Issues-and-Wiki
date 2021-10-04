package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneInteger implements Gene<Integer>
{
    protected Integer value = Integer.valueOf(0);

    @Override
    public Integer getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.value = tag.getInt("V");
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final Integer value)
    {
        this.value = value;
    }

}
