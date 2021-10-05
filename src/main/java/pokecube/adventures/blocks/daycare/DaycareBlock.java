package pokecube.adventures.blocks.daycare;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.block.ITickTile;

public class DaycareBlock extends InteractableHorizontalBlock implements EntityBlock
{

    public DaycareBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new DaycareTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }

    @Override
    public boolean isSignalSource(final BlockState state)
    {
        return true;
    }

    @Override
    public int getSignal(final BlockState blockState, final BlockGetter blockAccess, final BlockPos pos, final Direction side)
    {
        if (side == Direction.UP || side == Direction.DOWN) return 0;
        final BlockEntity tile = blockAccess.getBlockEntity(pos);
        if (tile instanceof DaycareTile) return ((DaycareTile) tile).redstonePower;
        return 0;
    }

    @Override
    public int getDirectSignal(final BlockState blockState, final BlockGetter blockAccess, final BlockPos pos,
            final Direction side)
    {
        if (side == Direction.UP || side == Direction.DOWN) return 0;
        final BlockEntity tile = blockAccess.getBlockEntity(pos);
        if (tile instanceof DaycareTile) return ((DaycareTile) tile).redstonePower;
        return 0;
    }

}
