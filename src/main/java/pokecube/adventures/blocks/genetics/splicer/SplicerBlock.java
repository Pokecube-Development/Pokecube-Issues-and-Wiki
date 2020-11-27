package pokecube.adventures.blocks.genetics.splicer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
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
import pokecube.adventures.blocks.genetics.extractor.ExtractorBlock;
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
				Block.makeCuboidShape(0, 0, 1, 16, 10, 15),
				Block.makeCuboidShape(0, 10, 10, 8, 16, 15),
				Block.makeCuboidShape(0, 10, 2, 8, 11, 10),
				Block.makeCuboidShape(10, 10, 7, 14, 11, 12),
				Block.makeCuboidShape(10, 10, 2, 14, 11, 6),
				Block.makeCuboidShape(10, 11, 9, 14, 12, 11),
				Block.makeCuboidShape(11, 10.98096988312782, 8.7, 13, 15.48096988312782, 10.7),
				Block.makeCuboidShape(10, 13.5, 3.5999999999999996, 14, 15, 10),
				Block.makeCuboidShape(10.7, 11.7, 2.9393398282201773, 13.2, 15.7, 4.939339828220177),
				Block.makeCuboidShape(1, 10.03806023374436, 1, 7, 15.03806023374436, 2),
				Block.makeCuboidShape(5, 11, 8, 6, 15, 9),
				Block.makeCuboidShape(6, 11, 6, 7, 15, 7),
				Block.makeCuboidShape(5, 11, 4, 6, 15, 5),
				Block.makeCuboidShape(1, 11, 6, 2, 15, 7)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.EAST,
    	        Stream.of(
    	                Block.makeCuboidShape(1, 0, 0, 15, 10, 16),
    	                Block.makeCuboidShape(1, 10, 0, 6, 16, 8),
    	                Block.makeCuboidShape(6, 10, 0, 14, 11, 8),
    	                Block.makeCuboidShape(4, 10, 10, 9, 11, 14),
    	                Block.makeCuboidShape(10, 10, 10, 14, 11, 14),
    	                Block.makeCuboidShape(5, 11, 10, 7, 12, 14),
    	                Block.makeCuboidShape(5.3, 10.98096988312782, 11, 7.3, 15.48096988312782, 13),
    	                Block.makeCuboidShape(6, 13.5, 10, 12.4, 15, 14),
    	                Block.makeCuboidShape(11.060660171779823, 11.7, 10.7, 13.060660171779823, 15.7, 13.2),
    	                Block.makeCuboidShape(14, 10.03806023374436, 1, 15, 15.03806023374436, 7),
    	                Block.makeCuboidShape(7, 11, 5, 8, 15, 6),
    	                Block.makeCuboidShape(9, 11, 6, 10, 15, 7),
    	                Block.makeCuboidShape(11, 11, 5, 12, 15, 6),
    	                Block.makeCuboidShape(9, 11, 1, 10, 15, 2)
    	                ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.SOUTH,
		Stream.of(
				Block.makeCuboidShape(0, 0, 1, 16, 10, 15),
				Block.makeCuboidShape(8, 10, 1, 16, 16, 6),
				Block.makeCuboidShape(8, 10, 6, 16, 11, 14),
				Block.makeCuboidShape(2, 10, 4, 6, 11, 9),
				Block.makeCuboidShape(2, 10, 10, 6, 11, 14),
				Block.makeCuboidShape(2, 11, 5, 6, 12, 7),
				Block.makeCuboidShape(3, 10.98096988312782, 5.300000000000001, 5, 15.48096988312782, 7.300000000000001),
				Block.makeCuboidShape(2, 13.5, 6, 6, 15, 12.4),
				Block.makeCuboidShape(2.8000000000000007, 11.7, 11.060660171779823, 5.300000000000001, 15.7, 13.060660171779823),
				Block.makeCuboidShape(9, 10.03806023374436, 14, 15, 15.03806023374436, 15),
				Block.makeCuboidShape(10, 11, 7, 11, 15, 8),
				Block.makeCuboidShape(9, 11, 9, 10, 15, 10),
				Block.makeCuboidShape(10, 11, 11, 11, 15, 12),
				Block.makeCuboidShape(14, 11, 9, 15, 15, 10)
				).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get()
        );
    	SplicerBlock.SPLICER.put(Direction.WEST,
		Stream.of(
				Block.makeCuboidShape(1, 0, 0, 15, 10, 16),
				Block.makeCuboidShape(10, 10, 8, 15, 16, 16),
				Block.makeCuboidShape(2, 10, 8, 10, 11, 16),
				Block.makeCuboidShape(7, 10, 2, 12, 11, 6),
				Block.makeCuboidShape(2, 10, 2, 6, 11, 6),
				Block.makeCuboidShape(9, 11, 2, 11, 12, 6),
				Block.makeCuboidShape(8.7, 10.98096988312782, 3, 10.7, 15.48096988312782, 5),
				Block.makeCuboidShape(3.5999999999999996, 13.5, 2, 10, 15, 6),
				Block.makeCuboidShape(2.9393398282201773, 11.7, 2.8000000000000007, 4.939339828220177, 15.7, 5.300000000000001),
				Block.makeCuboidShape(1, 10.03806023374436, 9, 2, 15.03806023374436, 15),
				Block.makeCuboidShape(8, 11, 10, 9, 15, 11),
				Block.makeCuboidShape(6, 11, 9, 7, 15, 10),
				Block.makeCuboidShape(4, 11, 10, 5, 15, 11),
				Block.makeCuboidShape(6, 11, 14, 7, 15, 15)
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

    public SplicerBlock(final Properties properties)
    {
        super(properties);
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
    public IFluidState getFluidState(BlockState state) 
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
