package pokecube.legends.blocks.customblocks;

import net.minecraft.util.StringRepresentable;

public enum XerneasCorePart implements StringRepresentable
{
    TOP("top"), TOP_LEFT("top_left"), TOP_RIGHT("top_right"),
    MIDDLE_LEFT("middle_left"), MIDDLE_RIGHT("middle_right"), BOTTOM("bottom");

    private final String name;

    XerneasCorePart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}