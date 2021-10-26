package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.util.StringRepresentable;

public enum ClonerBlockPart implements StringRepresentable
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
