package pokecube.core.blocks.repel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableBlock;

public class RepelBlock extends InteractableBlock
{

    public RepelBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new RepelTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = worldIn.getRedstonePowerFromNeighbors(pos);
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null || !(tile instanceof RepelTile)) return;
        final RepelTile repel = (RepelTile) tile;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
        }
    }
}
