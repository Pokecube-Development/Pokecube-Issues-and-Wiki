package pokecube.legends.blocks.customblocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import pokecube.legends.blocks.BlockBase;

public class HeatranBlock extends BlockBase
{
    public HeatranBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.STONE).hardnessAndResistance(1000, 1000));
    }
}