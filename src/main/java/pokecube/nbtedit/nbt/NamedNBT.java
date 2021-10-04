package pokecube.nbtedit.nbt;

import net.minecraft.nbt.Tag;

public class NamedNBT
{

    protected String name;
    protected Tag   nbt;

    public NamedNBT(Tag nbt)
    {
        this("", nbt);
    }

    public NamedNBT(String name, Tag nbt)
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

    public Tag getNBT()
    {
        return this.nbt;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setNBT(Tag nbt)
    {
        this.nbt = nbt;
    }

}
