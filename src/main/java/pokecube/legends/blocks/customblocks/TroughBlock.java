package pokecube.legends.blocks.customblocks;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class TroughBlock extends Rotates implements IWaterLoggable
{
    private static final Map<Direction, VoxelShape> TROUGH  = new HashMap<>();
    private static final DirectionProperty          FACING      = HorizontalBlock.FACING;
    private static final BooleanProperty            WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Precise selection box
    static
    {// @formatter:off
    	TroughBlock.TROUGH.put(Direction.NORTH,
          VoxelShapes.join(Block.box(12, 6, 4, 13, 11, 12),
            VoxelShapes.join(Block.box(3, 6, 4, 4, 11, 12),
              VoxelShapes.join(Block.box(3, 6, 3, 13, 11, 4),
                VoxelShapes.join(Block.box(3, 6, 12, 13, 11, 13),
                  VoxelShapes.join(Block.box(4, 5, 4, 12, 6, 12),
                    VoxelShapes.join(Block.box(6, 2, 6, 10, 5, 10),
                      VoxelShapes.join(Block.box(5, 3, 5, 11, 4, 11),
                        VoxelShapes.join(Block.box(5, 1, 5, 11, 2, 11),
                        		Block.box(4, 0, 4, 12, 1, 12),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	TroughBlock.TROUGH.put(Direction.EAST,
		VoxelShapes.join(Block.box(12, 6, 4, 13, 11, 12),
            VoxelShapes.join(Block.box(3, 6, 4, 4, 11, 12),
              VoxelShapes.join(Block.box(3, 6, 3, 13, 11, 4),
                VoxelShapes.join(Block.box(3, 6, 12, 13, 11, 13),
                  VoxelShapes.join(Block.box(4, 5, 4, 12, 6, 12),
                    VoxelShapes.join(Block.box(6, 2, 6, 10, 5, 10),
                      VoxelShapes.join(Block.box(5, 3, 5, 11, 4, 11),
                        VoxelShapes.join(Block.box(5, 1, 5, 11, 2, 11),
                        		Block.box(4, 0, 4, 12, 1, 12),
                                IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                          IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                    IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	TroughBlock.TROUGH.put(Direction.SOUTH,
		VoxelShapes.join(Block.box(12, 6, 4, 13, 11, 12),
        VoxelShapes.join(Block.box(3, 6, 4, 4, 11, 12),
          VoxelShapes.join(Block.box(3, 6, 3, 13, 11, 4),
            VoxelShapes.join(Block.box(3, 6, 12, 13, 11, 13),
              VoxelShapes.join(Block.box(4, 5, 4, 12, 6, 12),
                VoxelShapes.join(Block.box(6, 2, 6, 10, 5, 10),
                  VoxelShapes.join(Block.box(5, 3, 5, 11, 4, 11),
                    VoxelShapes.join(Block.box(5, 1, 5, 11, 2, 11),
                    		Block.box(4, 0, 4, 12, 1, 12),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR)
        );
    	TroughBlock.TROUGH.put(Direction.WEST,
		VoxelShapes.join(Block.box(12, 6, 4, 13, 11, 12),
        VoxelShapes.join(Block.box(3, 6, 4, 4, 11, 12),
          VoxelShapes.join(Block.box(3, 6, 3, 13, 11, 4),
            VoxelShapes.join(Block.box(3, 6, 12, 13, 11, 13),
              VoxelShapes.join(Block.box(4, 5, 4, 12, 6, 12),
                VoxelShapes.join(Block.box(6, 2, 6, 10, 5, 10),
                  VoxelShapes.join(Block.box(5, 3, 5, 11, 4, 11),
                    VoxelShapes.join(Block.box(5, 1, 5, 11, 2, 11),
                    		Block.box(4, 0, 4, 12, 1, 12),
                            IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                      IBooleanFunction.OR), IBooleanFunction.OR), IBooleanFunction.OR),
                IBooleanFunction.OR), IBooleanFunction.OR)
        );
    }// @formatter:on

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return TroughBlock.TROUGH.get(state.getValue(TroughBlock.FACING));
    }
    
    public TroughBlock(final String name, final Properties props)
    {
    	super(name, props);
    	this.registerDefaultState(this.stateDefinition.any().setValue(TroughBlock.FACING, Direction.NORTH).setValue(
    			TroughBlock.WATERLOGGED, false));
    }
}