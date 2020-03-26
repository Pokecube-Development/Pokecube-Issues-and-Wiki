package pokecube.legends.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class LegendaryBlock extends BlockBase
{
    public LegendaryBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.METAL).hardnessAndResistance(2000, 2000));
    }
}
