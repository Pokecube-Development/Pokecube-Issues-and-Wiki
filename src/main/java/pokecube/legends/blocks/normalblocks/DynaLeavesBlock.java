package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IForgeShearable;

public class DynaLeavesBlock extends LeavesBlock implements IForgeShearable
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;;

    public DynaLeavesBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(DynaLeavesBlock.SNOWY, false).setValue(DynaLeavesBlock.DISTANCE, 7).setValue(DynaLeavesBlock.PERSISTENT, false));
    }

    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1, final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        final int i = DynaLeavesBlock.getDistanceAt(state1) + 1;
        if (i != 1 || state.getValue(DynaLeavesBlock.DISTANCE) != i) world.scheduleTick(pos, this, 1);
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1) :
            (BlockState) state.setValue(DynaLeavesBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(Blocks.SNOW));
    }

    public static int getDistanceAt(final BlockState state) {
        if (state.is(BlockTags.LOGS)) return 0;
        else return state.getBlock() instanceof LeavesBlock ? (Integer)state.getValue(DynaLeavesBlock.DISTANCE) : 7;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
//      return (BlockState)this.defaultBlockState().setValue(SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW));

        return DynaLeavesBlock.updateDistance(this.defaultBlockState()
            .setValue(DynaLeavesBlock.SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW))
            .setValue(DynaLeavesBlock.PERSISTENT, true), context.getLevel(), context.getClickedPos());

    }

    public static BlockState updateDistance(final BlockState state, final LevelAccessor world, final BlockPos pos) {
        int i = 7;
        final BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        final Direction[] var5 = Direction.values();
        final int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            final Direction direction = var5[var7];
            blockpos$mutable.setWithOffset(pos, direction);
            i = Math.min(i, DynaLeavesBlock.getDistanceAt(world.getBlockState(blockpos$mutable)) + 1);
            if (i == 1) break;
        }

        return state.setValue(DynaLeavesBlock.DISTANCE, i);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(DynaLeavesBlock.DISTANCE, DynaLeavesBlock.PERSISTENT, DynaLeavesBlock.SNOWY);
    }
}
