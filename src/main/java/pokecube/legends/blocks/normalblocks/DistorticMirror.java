package pokecube.legends.blocks.normalblocks;

import pokecube.legends.blocks.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.SoundType;

public class DistorticMirror extends BlockBase
{		
  	
    public DistorticMirror(final String name, final Material material, MaterialColor color)
    {
        super(name, Properties.of(material).sound(SoundType.GLASS).strength(2, 4));
    }
}
