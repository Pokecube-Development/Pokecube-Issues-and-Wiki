package pokecube.legends.blocks.customblocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import pokecube.legends.blocks.BlockBase;

public class LegendaryBlock extends BlockBase
{
    public LegendaryBlock(final String name, final Material material, MaterialColor color)
    {
        super(name, Properties.of(material, color).sound(SoundType.METAL).strength(2000, 2000));
    }
}
