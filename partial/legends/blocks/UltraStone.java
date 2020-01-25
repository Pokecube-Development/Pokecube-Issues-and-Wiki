package pokecube.legends.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class UltraStone extends BlockBase 
{
	public UltraStone(String name, Material material) 
	{
		super(name, material);
		setSoundType(SoundType.STONE);
		setHardness(6.0F);
		setResistance(14.0F);
		setHarvestLevel("pickaxe", 1);
	}
}
