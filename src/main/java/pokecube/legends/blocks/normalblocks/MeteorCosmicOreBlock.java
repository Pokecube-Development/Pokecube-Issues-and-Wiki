package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MeteorCosmicOreBlock extends MeteorBlock
{
	private final UniformInt xpRange;

	public MeteorCosmicOreBlock(final int dustColor, final BlockBehaviour.Properties properties)
	{
	    this(dustColor, properties, UniformInt.of(0, 0));
	}

    public MeteorCosmicOreBlock(final int dustColor, final BlockBehaviour.Properties properties, UniformInt expDrops)
    {
        super(dustColor, properties);
        this.xpRange = expDrops;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader world, BlockPos pos, int fortune, int silktouch)
    {
       return silktouch == 0 ? this.xpRange.sample(RANDOM) : 0;
    }
}
