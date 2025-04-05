package pokecube.legends.blocks.customblocks;

import net.minecraft.util.StringRepresentable;

public enum TimeSpaceCorePart implements StringRepresentable
{
    TOP("top"), BOTTOM("bottom");

    private final String name;

    TimeSpaceCorePart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}