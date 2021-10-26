package pokecube.legends.blocks.customblocks;

import net.minecraft.util.StringRepresentable;

public enum VictiniBlockPart implements StringRepresentable
{
    TOP("top"), BOTTOM("bottom");

    private final String name;

    VictiniBlockPart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return this.name;
    }
}