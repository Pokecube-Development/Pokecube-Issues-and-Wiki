package pokecube.legends.blocks.customblocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import pokecube.legends.blocks.BlockBase;

public class TroughBlock extends BlockBase
{
    public TroughBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.METAL).hardnessAndResistance(1000, 1000));
    }
}