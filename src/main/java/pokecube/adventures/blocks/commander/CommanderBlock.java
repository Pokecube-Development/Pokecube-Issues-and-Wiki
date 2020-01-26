package pokecube.adventures.blocks.commander;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.blocks.tms.TMBlock;
import pokecube.core.interfaces.PokecubeMod;

public class CommanderBlock extends InteractableHorizontalBlock
{

    public CommanderBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new CommanderTile();
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

    private Direction getDir(final BlockPos pos, final BlockPos neighbor)
    {
        return Direction.UP;
    }

    @Override
    public void onNeighborChange(final BlockState state, final IWorldReader world, final BlockPos pos,
            final BlockPos neighbor)
    {
        final int power = world.getStrongPower(pos, this.getDir(pos, neighbor));
        final TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof CommanderTile)) return;
        final CommanderTile commander = (CommanderTile) tile;
        // Trigger on rising signal
        if (power > 0 && commander.power == 0) try
        {
            commander.initCommand();
            commander.sendCommand();
        }
        catch (final Exception e)
        {
            if (PokecubeMod.debug) PokecubeMod.LOGGER.warn("Invalid Commander Block use at " + pos, e);
            // TODO play some effects here to show it is broken.
        }
        commander.power = power;
    }

}
