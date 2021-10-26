package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.util.IStringSerializable;

public enum ClonerBlockPart implements IStringSerializable
{
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    ClonerBlockPart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}
