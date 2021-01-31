package pokecube.adventures.blocks.commander;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.interfaces.PokecubeMod;

public class CommanderBlock extends InteractableHorizontalBlock
{

    public CommanderBlock(final Properties properties, final MaterialColor color)
    {
        super(properties, color);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new CommanderTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block blockIn,
            final BlockPos neighbor, final boolean isMoving)
    {
        final int power = world.getRedstonePowerFromNeighbors(pos);
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
            PokecubeMod.LOGGER.warn("Invalid Commander Block use at " + pos + " " + e.getMessage());
        }
        commander.power = power;
    }

}
