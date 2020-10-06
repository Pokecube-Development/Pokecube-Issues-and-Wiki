package pokecube.legends.blocks.customblocks;

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
    public String getString()
    {
        return this.name;
    }
}