package pokecube.legends.blocks;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.trees.Tree;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public class SaplingBase extends BushBlock implements IGrowable
{

    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    protected static final VoxelShape   SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private final Supplier<Tree>        tree;

    public SaplingBase(final Supplier<Tree> treeIn, final Properties properties)
    {
        super(properties);
        this.tree = treeIn;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return SaplingBase.SHAPE;
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random rand)
    {
        if (!worldIn.isAreaLoaded(pos, 1)) return;
        if (worldIn.getMaxLocalRawBrightness(pos.above()) >= 9 && rand.nextInt(7) == 0) this.performBonemeal(worldIn,
                rand, pos, state);
    }

    public void grow(final ServerWorld serverWorld, final BlockPos pos, final BlockState state, final Random rand)
    {
        if (state.getValue(SaplingBase.STAGE) == 0) serverWorld.setBlock(pos, state.cycle(SaplingBase.STAGE), 4);
        else
        {
            if (!ForgeEventFactory.saplingGrowTree(serverWorld, rand, pos)) return;
            this.tree.get().growTree(serverWorld, serverWorld.getChunkSource().getGenerator(), pos, state, rand);
        }
    }

    @Override
    public void performBonemeal(final ServerWorld serverWorld, final Random rand, final BlockPos pos,
            final BlockState state)
    {
        this.grow(serverWorld, pos, state, rand);
    }

    @Override
    public boolean isValidBonemealTarget(final IBlockReader worldIn, final BlockPos pos, final BlockState state,
            final boolean isClient)
    {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(final World worldIn, final Random rand, final BlockPos pos, final BlockState state)
    {
        return worldIn.random.nextFloat() < 0.45D;
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder)
    {
        builder.add(SaplingBase.STAGE);
    }
}
