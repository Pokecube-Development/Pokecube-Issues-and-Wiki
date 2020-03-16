package pokecube.legends.blocks;

import net.minecraft.util.IStringSerializable;

public enum KeldeoBlockPart implements IStringSerializable
{
    TOP("top"), BOTTOM("bottom");

    private final String name;

    KeldeoBlockPart(final String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}