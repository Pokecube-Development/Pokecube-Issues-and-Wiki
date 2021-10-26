package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneTagCompound implements Gene<CompoundNBT>
{
    protected CompoundNBT value = new CompoundNBT();

    @Override
    public CompoundNBT getValue()
    {
        return this.value;
    }

    @Override
    public void load(final CompoundNBT tag)
    {
        this.value = tag.getCompound("V");
    }

    @Override
    public CompoundNBT save()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.put("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final CompoundNBT value)
    {
        this.value = value;
    }

}
