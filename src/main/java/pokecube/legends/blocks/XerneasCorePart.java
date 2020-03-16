package pokecube.legends.blocks;

import net.minecraft.util.IStringSerializable;

public enum XerneasCorePart implements IStringSerializable
{
    TOP("top"), TOP_LEFT("top_left"), TOP_RIGHT("top_right"), BOTTOM("bottom");

    private final String name;

    XerneasCorePart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}