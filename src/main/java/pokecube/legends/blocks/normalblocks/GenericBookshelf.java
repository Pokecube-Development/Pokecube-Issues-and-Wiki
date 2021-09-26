package pokecube.legends.blocks.containers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class GenericBookshelf extends Block
{
    public GenericBookshelf(final Properties props)
    {
    	super(props);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, IWorldReader world, BlockPos pos) {
        return 1f;
    }
}