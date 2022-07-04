package pokecube.legends.blocks.plants;

import java.util.Optional;
import java.util.Random;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;

public class BigContaminatedDripleafStemBlock extends BigDripleafStemBlock
        implements BonemealableBlock, SimpleWaterloggedBlock
{
    // Tag
    public static final TagKey<Block> BIG_CONTAMINATED_DRIPLEAF_PLACEABLE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "big_contaminated_dripleaf_placeable"));
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape NORTH_SHAPE = Block.box(4.0D, 0.0D, 8.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 8.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 4.0D, 8.0D, 16.0D, 12.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(8.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);

    public BigContaminatedDripleafStemBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
        switch ((Direction) state.getValue(FACING))
        {
        case SOUTH:
            return SOUTH_SHAPE;
        case NORTH:
        default:
            return NORTH_SHAPE;
        case WEST:
            return WEST_SHAPE;
        case EAST:
            return EAST_SHAPE;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockPos posBelow = pos.below();
        BlockState stateBelow = world.getBlockState(posBelow);
        BlockState stateAbove = world.getBlockState(pos.above());
        return (stateBelow.is(this) || stateBelow.is(BIG_CONTAMINATED_DRIPLEAF_PLACEABLE))
                && (stateAbove.is(this) || stateAbove.is(BlockInit.BIG_CONTAMINATED_DRIPLEAF.get()));
    }

    protected static boolean place(LevelAccessor world, BlockPos pos, FluidState fluidState, Direction direction)
    {
        BlockState state = BlockInit.BIG_CONTAMINATED_DRIPLEAF_STEM.get().defaultBlockState()
                .setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
                .setValue(FACING, direction);
        return world.setBlock(pos, state, 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world,
            BlockPos pos, BlockPos pos1)
    {
        if ((direction == Direction.DOWN || direction == Direction.UP) && !state.canSurvive(world, pos))
        {
            world.scheduleTick(pos, this, 1);
        }

        if (state.getValue(WATERLOGGED))
        {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, direction, state1, world, pos, pos1);
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (!state.canSurvive(world, pos))
        {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
    {
        Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(block, pos, state.getBlock(), Direction.UP,
                BlockInit.BIG_CONTAMINATED_DRIPLEAF.get());
        if (!optional.isPresent())
        {
            return false;
        }
        else
        {
            BlockPos posAbove = optional.get().above();
            BlockState stateAbove = block.getBlockState(posAbove);
            return BigContaminatedDripleafBlock.canPlaceAt(block, posAbove, stateAbove);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
        Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(world, pos, state.getBlock(), Direction.UP,
                BlockInit.BIG_CONTAMINATED_DRIPLEAF.get());
        if (optional.isPresent())
        {
            BlockPos posOptional = optional.get();
            BlockPos posAbove = posOptional.above();
            Direction direction = state.getValue(FACING);
            place(world, posOptional, world.getFluidState(posOptional), direction);
            BigContaminatedDripleafBlock.place(world, posAbove, world.getFluidState(posAbove), direction);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter block, BlockPos pos, BlockState state)
    {
        return new ItemStack(BlockInit.BIG_CONTAMINATED_DRIPLEAF.get());
    }
}