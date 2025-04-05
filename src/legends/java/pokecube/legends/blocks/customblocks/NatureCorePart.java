package pokecube.legends.blocks.customblocks;

import net.minecraft.util.StringRepresentable;

public enum NatureCorePart implements StringRepresentable
{
    TOP("top"), BOTTOM("bottom");

    private final String name;

    NatureCorePart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}