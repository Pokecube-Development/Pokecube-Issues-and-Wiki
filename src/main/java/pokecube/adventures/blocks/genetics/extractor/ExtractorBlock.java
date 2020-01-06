package pokecube.adventures.blocks.genetics.extractor;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.blocks.tms.TMBlock;

public class ExtractorBlock extends InteractableHorizontalBlock
{

    public ExtractorBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ExtractorTile();
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
