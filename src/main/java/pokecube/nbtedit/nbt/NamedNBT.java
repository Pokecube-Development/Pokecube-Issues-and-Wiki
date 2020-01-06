package pokecube.nbtedit.nbt;

import net.minecraft.nbt.INBT;

public class NamedNBT
{

    protected String name;
    protected INBT   nbt;

    public NamedNBT(INBT nbt)
    {
        this("", nbt);
    }

    public NamedNBT(String name, INBT nbt)
    {
        this.name = name;
        this.nbt = nbt;
    }

    public NamedNBT copy()
    {
        return new NamedNBT(this.name, this.nbt.copy());
    }

    public String getName()
    {
        return this.name;
    }

    public INBT getNBT()
    {
        return this.nbt;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNBT(INBT nbt)
    {
        this.nbt = nbt;
    }

}
