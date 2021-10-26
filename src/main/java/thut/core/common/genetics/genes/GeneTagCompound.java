package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundTag;
import thut.api.entity.genetics.Gene;

public abstract class GeneTagCompound implements Gene<CompoundTag>
{
    protected CompoundTag value = new CompoundTag();

    @Override
    public CompoundTag getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundTag tag)
    {
        this.value = tag.getCompound("V");
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.put("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final CompoundTag value)
    {
        this.value = value;
    }

}
