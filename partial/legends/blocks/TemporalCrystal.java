package pokecube.legends.blocks;

import java.util.Random;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import pokecube.legends.init.ItemInit;


public class TemporalCrystal extends BlockBase 
{
	public TemporalCrystal(String name, Material material) 
	{
		super(name, material);
		setSoundType(SoundType.GLASS);
		setHardness(5.0F);
		setResistance(15.0F);
		setHarvestLevel("pickaxe", 3);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ItemInit.CRYSTAL_SHARD;
	}
	
	@Override
	public int quantityDropped(Random rand) {
		int max = 2;
		int min = 0;
		return rand.nextInt(max) + min;
	}
}