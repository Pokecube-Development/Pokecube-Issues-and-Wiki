package pokecube.legends.blocks;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

public class SaplingBase extends BushBlock implements BonemealableBlock
{

    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    protected static final VoxelShape   SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private final Supplier<AbstractTreeGrower>        tree;

    public SaplingBase(final Supplier<AbstractTreeGrower> treeIn, final Properties properties)
    {
        super(properties.randomTicks());
        this.tree = treeIn;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        return SaplingBase.SHAPE;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random rand)
    {
        if (!worldIn.isPositionEntityTicking(pos)) return;
        if (worldIn.getMaxLocalRawBrightness(pos.above()) >= 0 && rand.nextInt(7) == 0) this.performBonemeal(worldIn,
                rand, pos, state);
    }

    public void grow(final ServerLevel serverWorld, final BlockPos pos, final BlockState state, final Random rand)
    {
        if (state.getValue(SaplingBase.STAGE) == 0) serverWorld.setBlock(pos, state.cycle(SaplingBase.STAGE), 4);
        else
        {
            if (!ForgeEventFactory.saplingGrowTree(serverWorld, rand, pos)) return;
            this.tree.get().growTree(serverWorld, serverWorld.getChunkSource().getGenerator(), pos, state, rand);
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
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder)
    {
        builder.add(SaplingBase.STAGE);
    }
}
