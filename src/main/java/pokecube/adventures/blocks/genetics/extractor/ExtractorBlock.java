package pokecube.adventures.blocks.genetics.extractor;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
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
import pokecube.core.blocks.InteractableHorizontalBlock;

public class ExtractorBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
	private static final Map<Direction, VoxelShape> EXTRACTOR  = new HashMap<>();
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty   FIXED  = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
 // Precise selection box
    static
    {// @formatter:off
    	ExtractorBlock.EXTRACTOR.put(Direction.NORTH,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 3, 16, 16, 16),
    					VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 0.6999999999999993, 0, 12.5, 3.6999999999999993, 6),
    							VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 8.100000000000001, 2.3393956609608875, 14, 16.1, 3.3393956609608875),
    									VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 3, 1, 5.5, 7, 2),
    											VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 1, 11.5, 7, 2),
    			Block.makeCuboidShape(7.5, 3, 1, 8.5, 7, 2),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	ExtractorBlock.EXTRACTOR.put(Direction.EAST,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 0, 13, 16, 16),
    					VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10, 0.6999999999999993, 3.5, 16, 3.6999999999999993, 12.5),
    							VoxelShapes.combineAndSimplify(Block.makeCuboidShape(12.660604339039113, 8.100000000000001, 2, 13.660604339039113, 16.1, 14),
    									VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14, 3, 4.5, 15, 7, 5.5),
    											VoxelShapes.combineAndSimplify(Block.makeCuboidShape(14, 3, 10.5, 15, 7, 11.5),
    			Block.makeCuboidShape(14, 3, 7.5, 15, 7, 8.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	ExtractorBlock.EXTRACTOR.put(Direction.SOUTH,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0, 0, 16, 16, 13),
    					VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3.5, 0.6999999999999993, 10, 12.5, 3.6999999999999993, 16),
    							VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2, 8.100000000000001, 12.660604339039113, 14, 16.1, 13.660604339039113),
    									VoxelShapes.combineAndSimplify(Block.makeCuboidShape(10.5, 3, 14, 11.5, 7, 15),
    											VoxelShapes.combineAndSimplify(Block.makeCuboidShape(4.5, 3, 14, 5.5, 7, 15),
    			Block.makeCuboidShape(7.5, 3, 14, 8.5, 7, 15),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	ExtractorBlock.EXTRACTOR.put(Direction.WEST,
    			VoxelShapes.combineAndSimplify(Block.makeCuboidShape(3, 0, 0, 16, 16, 16),
    					VoxelShapes.combineAndSimplify(Block.makeCuboidShape(0, 0.6999999999999993, 3.5, 6, 3.6999999999999993, 12.5),
    							VoxelShapes.combineAndSimplify(Block.makeCuboidShape(2.3393956609608875, 8.100000000000001, 2, 3.3393956609608875, 16.1, 14),
    									VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 3, 10.5, 2, 7, 11.5),
    											VoxelShapes.combineAndSimplify(Block.makeCuboidShape(1, 3, 4.5, 2, 7, 5.5),
    			Block.makeCuboidShape(1, 3, 7.5, 2, 7, 8.5),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return ExtractorBlock.EXTRACTOR.get(state.get(ExtractorBlock.FACING));
    }
    
    public ExtractorBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
        this.setDefaultState(this.stateContainer.getBaseState().with(ExtractorBlock.FACING, Direction.NORTH).with(
        		ExtractorBlock.FIXED, false).with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ExtractorBlock.FACING);
        builder.add(ExtractorBlock.FIXED);
        builder.add(ExtractorBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(ExtractorBlock.FACING, context.getPlacementHorizontalFacing().getOpposite())
                .with(ExtractorBlock.FIXED, false).with(WATERLOGGED, flag);
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
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ExtractorTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

}
