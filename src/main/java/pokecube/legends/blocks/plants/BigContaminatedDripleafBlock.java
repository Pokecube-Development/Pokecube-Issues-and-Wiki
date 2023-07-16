package pokecube.legends.blocks.plants;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;

public class BigContaminatedDripleafBlock extends BigDripleafBlock implements BonemealableBlock, SimpleWaterloggedBlock
{
    // Tag
    public static final TagKey<Block> BIG_CONTAMINATED_DRIPLEAF_PLACEABLE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation(Reference.ID, "big_contaminated_dripleaf_placeable"));
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
    private static final Map<Tilt, VoxelShape> LEAF_SHAPES = ImmutableMap.of(Tilt.NONE,
            Block.box(0.0D, 11.0D, 0.0D, 16.0D, 15.0D, 16.0D), Tilt.UNSTABLE,
            Block.box(0.0D, 11.0D, 0.0D, 16.0D, 15.0D, 16.0D), Tilt.PARTIAL,
            Block.box(0.0D, 11.0D, 0.0D, 16.0D, 13.0D, 16.0D), Tilt.FULL, Shapes.empty());
    private static final VoxelShape STEM_SLICER = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final Map<Direction, VoxelShape> STEM_SHAPES = ImmutableMap.of(Direction.NORTH,
            Shapes.joinUnoptimized(BigContaminatedDripleafStemBlock.NORTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
            Direction.SOUTH,
            Shapes.joinUnoptimized(BigContaminatedDripleafStemBlock.SOUTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
            Direction.EAST,
            Shapes.joinUnoptimized(BigContaminatedDripleafStemBlock.EAST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST),
            Direction.WEST,
            Shapes.joinUnoptimized(BigContaminatedDripleafStemBlock.WEST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST));
    private final Map<BlockState, VoxelShape> shapesCache;

    public BigContaminatedDripleafBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE));
        this.shapesCache = this.getShapeForEachState(BigContaminatedDripleafBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state)
    {
        return Shapes.or(LEAF_SHAPES.get(state.getValue(TILT)), STEM_SHAPES.get(state.getValue(FACING)));
    }

    public static void placeWithRandomHeight(LevelAccessor world, Random random, BlockPos pos, Direction direction)
    {
        int i = Mth.nextInt(random, 2, 5);
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        int j = 0;

        while (j < i && canPlaceAt(world, mutablePos, world.getBlockState(mutablePos)))
        {
            ++j;
            mutablePos.move(Direction.UP);
        }

        int k = pos.getY() + j - 1;
        mutablePos.setY(pos.getY());

        while (mutablePos.getY() < k)
        {
            BigContaminatedDripleafStemBlock.place(world, mutablePos, world.getFluidState(mutablePos), direction);
            mutablePos.move(Direction.UP);
        }
        place(world, mutablePos, world.getFluidState(mutablePos), direction);
    }

    private static boolean canReplace(BlockState state)
    {
        return state.isAir() || state.is(Blocks.WATER) || state.is(BlockInit.SMALL_CONTAMINATED_DRIPLEAF.get());
    }

    protected static boolean canPlaceAt(LevelHeightAccessor height, BlockPos pos, BlockState state)
    {
        return !height.isOutsideBuildHeight(pos) && canReplace(state);
    }

    protected static boolean place(LevelAccessor world, BlockPos pos, FluidState fluidState, Direction direction)
    {
        BlockState state = BlockInit.BIG_CONTAMINATED_DRIPLEAF.get().defaultBlockState()
                .setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
                .setValue(FACING, direction);
        return world.setBlock(pos, state, 3);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
        BlockPos posBelow = pos.below();
        BlockState stateBelow = world.getBlockState(posBelow);
        return stateBelow.is(this) || stateBelow.is(BlockInit.BIG_CONTAMINATED_DRIPLEAF_STEM.get())
                || stateBelow.is(BIG_CONTAMINATED_DRIPLEAF_PLACEABLE);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter block, BlockPos pos, BlockState state, boolean b)
    {
        BlockState stateAbove = block.getBlockState(pos.above());
        return canReplace(stateAbove);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world,
            BlockPos pos, BlockPos pos1)
    {
        if (direction == Direction.DOWN && !state.canSurvive(world, pos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            if (state.getValue(WATERLOGGED))
            {
                world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
            }

            return direction == Direction.UP && state1.is(this)
                    ? BlockInit.BIG_CONTAMINATED_DRIPLEAF_STEM.get().withPropertiesOf(state)
                    : super.updateShape(state, direction, state1, world, pos, pos1);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, Random random, BlockPos pos, BlockState state)
    {
        BlockPos posAbove = pos.above();
        BlockState stateAbove = world.getBlockState(posAbove);
        if (canPlaceAt(world, posAbove, stateAbove))
        {
            Direction direction = state.getValue(FACING);
            BigContaminatedDripleafStemBlock.place(world, pos, state.getFluidState(), direction);
            place(world, posAbove, stateAbove.getFluidState(), direction);
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
        return this.shapesCache.get(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().below());
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = state.is(BlockInit.BIG_CONTAMINATED_DRIPLEAF.get())
                || state.is(BlockInit.BIG_CONTAMINATED_DRIPLEAF_STEM.get());
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
                .setValue(FACING, flag ? state.getValue(FACING) : context.getHorizontalDirection().getOpposite());
    }
}