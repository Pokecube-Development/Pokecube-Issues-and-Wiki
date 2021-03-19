package pokecube.adventures.blocks.genetics.extractor;

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
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import pokecube.core.blocks.InteractableHorizontalBlock;

import java.util.HashMap;
import java.util.Map;

public class ExtractorBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
	private static final Map<Direction, VoxelShape> EXTRACTOR  = new HashMap<>();
    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final BooleanProperty   FIXED  = BooleanProperty.create("fixed");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
 // Precise selection box
    static
    {
    	ExtractorBlock.EXTRACTOR.put(Direction.NORTH, VoxelShapes.or(
    		Block.box(0, 0, 3, 16, 16, 16),
    		Block.box(3.5, 0, 0, 12.5, 3, 6),
    		Block.box(1, 8, 2, 15, 16, 3),
    		Block.box(4.5, 3, 1, 5.5, 7, 2),
    		Block.box(10.5, 3, 1, 11.5, 7, 2),
    		Block.box(7.5, 3, 1, 8.5, 7, 2)).optimize());
    	ExtractorBlock.EXTRACTOR.put(Direction.EAST, VoxelShapes.or(
    		Block.box(0, 0, 0, 13, 16, 16),
    		Block.box(10, 0, 3.5, 16, 3, 12.5),
    		Block.box(13, 8, 1, 14, 16, 15),
    		Block.box(14, 3, 4.5, 15, 7, 5.5),
    		Block.box(14, 3, 10.5, 15, 7, 11.5),
    		Block.box(14, 3, 7.5, 15, 7, 8.5)).optimize());
    	ExtractorBlock.EXTRACTOR.put(Direction.SOUTH, VoxelShapes.or(
    		Block.box(0, 0, 0, 16, 16, 13),
    		Block.box(3.5, 0, 10, 12.5, 3, 16),
    		Block.box(1, 8, 13, 15, 16, 14),
    		Block.box(10.5, 3, 14, 11.5, 7, 15),
    		Block.box(4.5, 3, 14, 5.5, 7, 15),
    		Block.box(7.5, 3, 14, 8.5, 7, 15)).optimize());
    	ExtractorBlock.EXTRACTOR.put(Direction.WEST, VoxelShapes.or(
    		Block.box(3, 0, 0, 16, 16, 16),
    		Block.box(0, 0, 3.5, 6, 3, 12.5),
    		Block.box(2, 8, 1, 3, 16, 15),
    		Block.box(1, 3, 10.5, 2, 7, 11.5),
    		Block.box(1, 3, 4.5, 2, 7, 5.5),
    		Block.box(1, 3, 7.5, 2, 7, 8.5)).optimize());
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return ExtractorBlock.EXTRACTOR.get(state.getValue(ExtractorBlock.FACING));
    }
    
    public ExtractorBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ExtractorBlock.FACING, Direction.NORTH).setValue(
        		ExtractorBlock.FIXED, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ExtractorBlock.FACING);
        builder.add(ExtractorBlock.FIXED);
        builder.add(ExtractorBlock.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(ExtractorBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ExtractorBlock.FIXED, false).setValue(WATERLOGGED, flag);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
            BlockPos facingPos) 
    {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) 
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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
