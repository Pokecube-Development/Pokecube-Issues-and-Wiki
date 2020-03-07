package pokecube.adventures.blocks.genetics.cloner;

import net.minecraft.util.IStringSerializable;

public enum ClonerBlockPart implements IStringSerializable
{
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    ClonerBlockPart(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
}
