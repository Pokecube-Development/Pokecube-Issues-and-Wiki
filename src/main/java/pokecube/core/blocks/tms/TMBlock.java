package pokecube.core.blocks.tms;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableHorizontalBlock;

public class TMBlock extends InteractableHorizontalBlock
{
    public static final VoxelShape PARTIAL_BASE  = Block.makeCuboidShape(0.05D, 0.0D, 0.05D, 15.95D, 2.0D, 15.95D);
    public static final VoxelShape CENTRALCOLUMN = Block.makeCuboidShape(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
    public static final VoxelShape RENDERSHAPE   = VoxelShapes.or(TMBlock.PARTIAL_BASE, TMBlock.CENTRALCOLUMN);

    public TMBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TMTile();
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return TMBlock.RENDERSHAPE;
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }
}
