package pokecube.adventures.blocks.commander;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.interfaces.PokecubeMod;

public class CommanderBlock extends InteractableHorizontalBlock
{

    public CommanderBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
    {
        return new CommanderTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos, final Block blockIn,
            final BlockPos neighbor, final boolean isMoving)
    {
        final int power = world.getBestNeighborSignal(pos);
        final BlockEntity tile = world.getBlockEntity(pos);
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
