package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundTag;
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
    public void load(final CompoundTag tag)
    {
        this.value = tag.getString("V");
    }

    @Override
    public CompoundTag save()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putString("V", this.value);
        return tag;
    }

    @Override
    public void setValue(final String value)
    {
        this.value = value;
    }

}
