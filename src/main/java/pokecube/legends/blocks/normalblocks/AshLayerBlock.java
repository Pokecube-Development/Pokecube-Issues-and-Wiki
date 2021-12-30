package pokecube.legends.blocks.normalblocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.blocks.FallingBlockBase;

public class AshLayerBlock extends FallingBlockBase implements Fallable
{
    public static final int MAX_HEIGHT = 16;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 16);
    public static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{Shapes.empty(), 
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 11.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};
    public static final int HEIGHT_IMPASSABLE = 10;
    
    public AshLayerBlock(final int color, final Properties properties)
    {
        super(color, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter block, BlockPos pos, PathComputationType path)
    {
       switch(path)
       {
       case LAND:
          return state.getValue(LAYERS) < 10;
       case WATER:
          return false;
       case AIR:
          return false;
       default:
          return false;
       }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
       return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
       return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter block, BlockPos pos)
    {
       return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
       return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state)
    {
       return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
       BlockState state1 = world.getBlockState(pos.below());
       if (!state1.is(Blocks.BARRIER))
       {
          if (!state1.is(Blocks.HONEY_BLOCK))
          {
             return Block.isFaceFull(state1.getCollisionShape(world, pos.below()), Direction.UP)
                     || state1.is(this) && state1.getValue(LAYERS) == 16;
          } else
          {
             return true;
          }
       } else
       {
          return false;
       }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
    {
        return !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, state1, world, pos, pos1);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        int i = state.getValue(LAYERS);
        if (context.getItemInHand().is(this.asItem()) && i < 16)
        {
           if (context.replacingClickedOnBlock())
           {
              return context.getClickedFace() == Direction.UP;
           } else
           {
              return true;
           }
        } else
        {
           return i < 10;
        }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
       BlockState state = context.getLevel().getBlockState(context.getClickedPos());
       if (state.is(this))
       {
          int i = state.getValue(LAYERS);
          return state.setValue(LAYERS, Integer.valueOf(Math.min(16, i + 1)));
       } else
       {
          return super.getStateForPlacement(context);
       }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
       builder.add(LAYERS);
    }
}
