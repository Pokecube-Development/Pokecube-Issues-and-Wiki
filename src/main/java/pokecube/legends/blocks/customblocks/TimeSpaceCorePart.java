package pokecube.legends.blocks.customblocks;

import net.minecraft.util.IStringSerializable;

public enum TimeSpaceCorePart implements IStringSerializable
{
    TOP("top"), BOTTOM("bottom");

    private final String name;

    TimeSpaceCorePart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}