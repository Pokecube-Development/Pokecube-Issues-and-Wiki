package pokecube.adventures.blocks.daycare;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class DaycareBlock extends InteractableHorizontalBlock
{

    public DaycareBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new DaycareTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public boolean canProvidePower(final BlockState state)
    {
        return true;
    }

    @Override
    public int getWeakPower(final BlockState blockState, final IBlockReader blockAccess, final BlockPos pos, final Direction side)
    {
        if (side == Direction.UP || side == Direction.DOWN) return 0;
        final TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile instanceof DaycareTile) return ((DaycareTile) tile).redstonePower;
        return 0;
    }

    @Override
    public int getStrongPower(final BlockState blockState, final IBlockReader blockAccess, final BlockPos pos,
            final Direction side)
    {
        if (side == Direction.UP || side == Direction.DOWN) return 0;
        final TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile instanceof DaycareTile) return ((DaycareTile) tile).redstonePower;
        return 0;
    }

}
