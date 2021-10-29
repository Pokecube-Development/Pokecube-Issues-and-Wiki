package pokecube.legends.blocks.plants;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.event.ForgeEventFactory;
import pokecube.legends.blocks.SaplingBase;

public class MirageSapling extends SaplingBlock implements BonemealableBlock
{
	private final AbstractTreeGrower treeGrower;

	public MirageSapling(final AbstractTreeGrower tree, final Properties properties)
    {
	    super(tree, properties.randomTicks());
		this.treeGrower = tree;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random rand)
    {
        if (!worldIn.isAreaLoaded(pos, 1)) return;
        if (worldIn.getMaxLocalRawBrightness(pos.above()) >= 0 && rand.nextInt(7) == 0) this.performBonemeal(worldIn,
                rand, pos, state);
    }

    public void grow(final ServerLevel serverWorld, final BlockPos pos, final BlockState state, final Random rand)
    {
        if (state.getValue(SaplingBase.STAGE) == 0) serverWorld.setBlock(pos, state.cycle(SaplingBase.STAGE), 4);
        else
        {
            if (!ForgeEventFactory.saplingGrowTree(serverWorld, rand, pos)) return;
            this.treeGrower.growTree(serverWorld, serverWorld.getChunkSource().getGenerator(), pos, state, rand);
        }
    }

    @Override
    public void performBonemeal(final ServerLevel serverWorld, final Random rand, final BlockPos pos,
            final BlockState state)
    {
        this.grow(serverWorld, pos, state, rand);
    }

    @Override
    public boolean isValidBonemealTarget(final BlockGetter worldIn, final BlockPos pos, final BlockState state,
            final boolean isClient)
    {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(final Level worldIn, final Random rand, final BlockPos pos, final BlockState state)
    {
        return worldIn.random.nextFloat() < 0.45D;
    }

	@Override
	public PlantType getPlantType(final BlockGetter world, final BlockPos pos)
	{
	    return PlantType.DESERT;
	}

	@Override
	protected boolean mayPlaceOn(final BlockState state, final BlockGetter block, final BlockPos pos)
	{
		return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(Blocks.FARMLAND);
	}
}