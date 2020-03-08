package pokecube.core.blocks.maxspot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class MaxBlock extends InteractableHorizontalBlock
{
    public static final VoxelShape PARTIAL_BASE  = Block.makeCuboidShape(0.05D, 0.0D, 0.05D, 15.95D, 2.0D, 15.95D);
    public static final VoxelShape CENTRALCOLUMN = Block.makeCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 6.0D, 12.0D);
    public static final VoxelShape RENDERSHAPE   = VoxelShapes.or(MaxBlock.PARTIAL_BASE, MaxBlock.CENTRALCOLUMN);

    public MaxBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new MaxTile();
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return MaxBlock.RENDERSHAPE;
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
        if (tile == null || !(tile instanceof MaxTile)) return;
        final MaxTile repel = (MaxTile) tile;
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
