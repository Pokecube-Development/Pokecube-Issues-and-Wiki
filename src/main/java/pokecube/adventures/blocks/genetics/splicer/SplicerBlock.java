package pokecube.adventures.blocks.genetics.splicer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import pokecube.core.blocks.InteractableBlock;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class SplicerBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> SPLICER = new HashMap<>();
    public static final DirectionProperty           FACING  = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty             FIXED   = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	SplicerBlock.SPLICER.put(Direction.NORTH,
		Stream.of(
				Block.makeCuboidShape(0, 0, 3, 16, 10, 16),
				Block.makeCuboidShape(0, 10, 11, 8, 16, 16),
				Block.makeCuboidShape(10, 10, 11, 14, 12, 15),
				Block.makeCuboidShape(10, 10, 4, 14, 11, 8),
				Block.makeCuboidShape(11, 12, 12, 13, 16, 14),
				Block.makeCuboidShape(10, 13.5, 6, 14, 15.5, 13),
				Block.makeCuboidShape(11, 12, 5, 13, 16, 7),
				Block.makeCuboidShape(1, 10, 3, 7, 15, 4),
				Block.makeCuboidShape(0, 10, 4, 8, 11, 11),
				Block.makeCuboidShape(5, 11, 9, 6, 15, 10),
				Block.makeCuboidShape(6, 11, 7, 7, 15, 8),
				Block.makeCuboidShape(5, 11, 5, 6, 15, 6),
				Block.makeCuboidShape(1, 11, 7, 2, 15, 8)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.EAST,
    	Stream.of(
    	        Block.makeCuboidShape(0, 0, 0, 13, 10, 16),
    	        Block.makeCuboidShape(0, 10, 0, 5, 16, 8),
    	        Block.makeCuboidShape(1, 10, 10, 5, 12, 14),
    	        Block.makeCuboidShape(8, 10, 10, 12, 11, 14),
    	        Block.makeCuboidShape(2, 12, 11, 4, 16, 13),
    	        Block.makeCuboidShape(3, 13.5, 10, 10, 15.5, 14),
    	        Block.makeCuboidShape(9, 12, 11, 11, 16, 13),
    	        Block.makeCuboidShape(12, 10, 1, 13, 15, 7),
    	        Block.makeCuboidShape(5, 10, 0, 12, 11, 8),
    	        Block.makeCuboidShape(6, 11, 5, 7, 15, 6),
    	        Block.makeCuboidShape(8, 11, 6, 9, 15, 7),
    	        Block.makeCuboidShape(10, 11, 5, 11, 15, 6),
    	        Block.makeCuboidShape(8, 11, 1, 9, 15, 2)
    	        ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.SOUTH,
		Stream.of(
				Block.makeCuboidShape(0, 0, 0, 16, 10, 13),
				Block.makeCuboidShape(8, 10, 0, 16, 16, 5),
				Block.makeCuboidShape(2, 10, 1, 6, 12, 5),
				Block.makeCuboidShape(2, 10, 8, 6, 11, 12),
				Block.makeCuboidShape(3, 12, 2, 5, 16, 4),
				Block.makeCuboidShape(2, 13.5, 3, 6, 15.5, 10),
				Block.makeCuboidShape(3, 12, 9, 5, 16, 11),
				Block.makeCuboidShape(9, 10, 12, 15, 15, 13),
				Block.makeCuboidShape(8, 10, 5, 16, 11, 12),
				Block.makeCuboidShape(10, 11, 6, 11, 15, 7),
				Block.makeCuboidShape(9, 11, 8, 10, 15, 9),
				Block.makeCuboidShape(10, 11, 10, 11, 15, 11),
				Block.makeCuboidShape(14, 11, 8, 15, 15, 9)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.WEST,
		Stream.of(
				Block.makeCuboidShape(3, 0, 0, 16, 10, 16),
				Block.makeCuboidShape(11, 10, 8, 16, 16, 16),
				Block.makeCuboidShape(11, 10, 2, 15, 12, 6),
				Block.makeCuboidShape(4, 10, 2, 8, 11, 6),
				Block.makeCuboidShape(12, 12, 3, 14, 16, 5),
				Block.makeCuboidShape(6, 13.5, 2, 13, 15.5, 6),
				Block.makeCuboidShape(5, 12, 3, 7, 16, 5),
				Block.makeCuboidShape(3, 10, 9, 4, 15, 15),
				Block.makeCuboidShape(4, 10, 8, 11, 11, 16),
				Block.makeCuboidShape(9, 11, 10, 10, 15, 11),
				Block.makeCuboidShape(7, 11, 9, 8, 15, 10),
				Block.makeCuboidShape(5, 11, 10, 6, 15, 11),
				Block.makeCuboidShape(7, 11, 14, 8, 15, 15)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return SplicerBlock.SPLICER.get(state.get(SplicerBlock.FACING));
    }

    public SplicerBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
        this.setDefaultState(this.stateContainer.getBaseState().with(SplicerBlock.FACING, Direction.NORTH).with(
                SplicerBlock.FIXED, false).with(WATERLOGGED, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new SplicerTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(SplicerBlock.FACING);
        builder.add(SplicerBlock.FIXED);
        builder.add(SplicerBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(SplicerBlock.FACING, context.getPlacementHorizontalFacing().getOpposite())
                .with(SplicerBlock.FIXED, false).with(WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
            BlockPos facingPos) 
    {
        if (state.get(WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) 
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return InteractableBlock.RENDERSHAPE;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
