package pokecube.legends.blocks.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.init.BlockInit;

public class DistorticSapling extends SaplingBlock
{
	public DistorticSapling(final TreeGrower tree, final Properties properties)
    {
	    super(tree, properties.randomTicks());
    }

	@Override
	protected boolean mayPlaceOn(final BlockState state, final BlockGetter block, final BlockPos pos)
	{
		return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(BlockInit.DISTORTIC_STONE.get());
	}
}