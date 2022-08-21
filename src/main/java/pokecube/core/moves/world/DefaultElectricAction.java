package pokecube.core.moves.world;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.Move_Base;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import thut.api.maths.Vector3;

public class DefaultElectricAction extends DefaultAction
{

    public DefaultElectricAction(Move_Base move)
    {
        super(move);
    }

    @Override
    /**
     * This will have the following effects, for "Strong" electric type moves:
     * Melt sand to glass
     */
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (move.getPWR() < MoveEventsHandler.ELECTRICSTRONG || !PokecubeCore.getConfig().defaultElectricActions)
            return false;
        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        final Level world = user.getEntity().getLevel();
        final BlockState state = location.getBlockState(world);
        final Block block = state.getBlock();
        final Vector3 nextBlock = new Vector3().set(user.getEntity()).subtractFrom(location).reverse().norm()
                .addTo(location);
        final BlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.SAND)
        {
            location.setBlock(world, Blocks.GLASS.defaultBlockState());
            return true;
        }
        else if (state
                .canBeReplaced(MoveEventsHandler.getContext(world, user, Blocks.GLASS.defaultBlockState(), location))
                && nextState.getBlock() == Blocks.SAND)
        {
            nextBlock.setBlock(world, Blocks.GLASS.defaultBlockState());
            return true;
        }
        return false;
    }

}
