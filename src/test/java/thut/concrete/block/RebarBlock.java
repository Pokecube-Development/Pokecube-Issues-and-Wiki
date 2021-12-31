package thut.concrete.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import thut.concrete.Concrete;
import thut.concrete.fluid.ConcreteFluid;

public class RebarBlock extends CrossCollisionBlock implements SimpleWaterloggedBlock
{
    private final VoxelShape[] occlusionByIndex;

    public static final BooleanProperty FLUIDLOGGED = BooleanProperty.create("fluidlogged");

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 8);

    public RebarBlock(Properties properties)
    {
        super(2.0F, 2.0F, 16.0F, 16.0F, 16.0F, properties);
        registerDefaultState(getStateDefinition().any().setValue(FLUIDLOGGED, false).setValue(LEVEL, 0));
        this.occlusionByIndex = this.makeShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, FLUIDLOGGED, WATERLOGGED, LEVEL);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_53338_, BlockGetter p_53339_, BlockPos p_53340_)
    {
        return this.occlusionByIndex[this.getAABBIndex(p_53338_)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState p_53311_, BlockGetter p_53312_, BlockPos p_53313_,
            CollisionContext p_53314_)
    {
        return this.getShape(p_53311_, p_53312_, p_53313_, p_53314_);
    }

    public boolean connectsTo(BlockState p_53330_, boolean p_53331_, Direction p_53332_)
    {
        Block block = p_53330_.getBlock();
        boolean flag = this.isSameFence(p_53330_);
        boolean flag1 = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(p_53330_, p_53332_);
        return !isExceptionForConnection(p_53330_) && p_53331_ || flag || flag1;
    }

    private boolean isSameFence(BlockState p_153255_)
    {
        return p_153255_.getBlock()==this;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_53304_)
    {
        BlockGetter blockgetter = p_53304_.getLevel();
        BlockPos blockpos = p_53304_.getClickedPos();
        FluidState fluidstate = p_53304_.getLevel().getFluidState(p_53304_.getClickedPos());
        BlockPos blockpos1 = blockpos.north();
        BlockPos blockpos2 = blockpos.east();
        BlockPos blockpos3 = blockpos.south();
        BlockPos blockpos4 = blockpos.west();
        BlockState blockstate = blockgetter.getBlockState(blockpos1);
        BlockState blockstate1 = blockgetter.getBlockState(blockpos2);
        BlockState blockstate2 = blockgetter.getBlockState(blockpos3);
        BlockState blockstate3 = blockgetter.getBlockState(blockpos4);
        return super.getStateForPlacement(p_53304_)
                .setValue(
                        NORTH,
                        Boolean.valueOf(this.connectsTo(blockstate,
                                blockstate.isFaceSturdy(blockgetter, blockpos1, Direction.SOUTH), Direction.SOUTH)))
                .setValue(EAST,
                        Boolean.valueOf(this.connectsTo(blockstate1,
                                blockstate1.isFaceSturdy(blockgetter, blockpos2, Direction.WEST), Direction.WEST)))
                .setValue(SOUTH,
                        Boolean.valueOf(this.connectsTo(blockstate2,
                                blockstate2.isFaceSturdy(blockgetter, blockpos3, Direction.NORTH), Direction.NORTH)))
                .setValue(WEST,
                        Boolean.valueOf(this.connectsTo(blockstate3,
                                blockstate3.isFaceSturdy(blockgetter, blockpos4, Direction.EAST), Direction.EAST)))
                .setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState p_53323_, Direction p_53324_, BlockState p_53325_, LevelAccessor p_53326_,
            BlockPos p_53327_, BlockPos p_53328_)
    {
        if (p_53323_.getValue(FLUIDLOGGED))
        {
            p_53326_.scheduleTick(p_53327_, Concrete.CONCRETE_FLUID_BLOCK.get(), 10);
        }
        return p_53324_.getAxis().getPlane() == Direction.Plane.HORIZONTAL
                ? p_53323_.setValue(PROPERTY_BY_DIRECTION.get(p_53324_), Boolean.valueOf(this.connectsTo(p_53325_,
                        p_53325_.isFaceSturdy(p_53326_, p_53328_, p_53324_.getOpposite()), p_53324_.getOpposite())))
                : super.updateShape(p_53323_, p_53324_, p_53325_, p_53326_, p_53327_, p_53328_);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
    {
        return !state.getValue(FLUIDLOGGED) && fluidIn == Concrete.CONCRETE_FLUID.get();
    }

    @Override
    public boolean placeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn)
    {
        if (canPlaceLiquid(worldIn, pos, state, fluidStateIn.getType()))
        {
            if (!worldIn.isClientSide())
            {
                int amt = fluidStateIn.getAmount();
                
                
                worldIn.setBlock(pos, state.setValue(FLUIDLOGGED, true).setValue(LEVEL, amt), 3);
                worldIn.scheduleTick(pos, fluidStateIn.getType(), fluidStateIn.getType().getTickDelay(worldIn));
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor worldIn, BlockPos pos, BlockState state)
    {
        if (state.getValue(FLUIDLOGGED))
        {
            worldIn.setBlock(pos, state.setValue(FLUIDLOGGED, false), 3);
            return new ItemStack(Concrete.CONCRETE_BUCKET.get());
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        if (!state.getValue(FLUIDLOGGED))
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
        int amt = state.getValue(LEVEL);
        if (amt == 0)
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
        return Concrete.CONCRETE_FLUID.get().defaultFluidState().setValue(ConcreteFluid.LEVEL, amt);
    }
}
