package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.*;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.IForgeShearable;

public class DynaLeavesBlock extends LeavesBlock implements IForgeShearable
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;;

    public DynaLeavesBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
            .setValue(SNOWY, false)).setValue(DISTANCE, 7)).setValue(PERSISTENT, false));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, IWorld world, BlockPos pos, BlockPos pos1)
    {
        int i = getDistanceAt(state1) + 1;
        if (i != 1 || (Integer)state.getValue(DISTANCE) != i) {
            world.getBlockTicks().scheduleTick(pos, this, 1);
        }
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
            (BlockState) state.setValue(SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW));
//        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
//            (BlockState) state.setValue(SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW));
    }

    public static int getDistanceAt(BlockState state) {
        if (BlockTags.LOGS.contains(state.getBlock())) {
            return 0;
        } else {
            return state.getBlock() instanceof LeavesBlock ? (Integer)state.getValue(DISTANCE) : 7;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
//      return (BlockState)this.defaultBlockState().setValue(SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW));

        return updateDistance((BlockState) this.defaultBlockState()
            .setValue(SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW))
            .setValue(PERSISTENT, true), context.getLevel(), context.getClickedPos());

    }

    public static BlockState updateDistance(BlockState state, IWorld world, BlockPos pos) {
        int i = 7;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        Direction[] var5 = Direction.values();
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            Direction direction = var5[var7];
            blockpos$mutable.setWithOffset(pos, direction);
            i = Math.min(i, getDistanceAt(world.getBlockState(blockpos$mutable)) + 1);
            if (i == 1) {
                break;
            }
        }

        return (BlockState)state.setValue(DISTANCE, i);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(DISTANCE, PERSISTENT, SNOWY);
    }
}
