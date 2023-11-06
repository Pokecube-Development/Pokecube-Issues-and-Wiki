package pokecube.core.moves.world;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class DefaultIceAction extends DefaultAction
{

    public DefaultIceAction(MoveEntry move)
    {
        super(move);
    }

    @Override
    /**
     * This will have the following effects, for ice type moves: Place snow
     * Freeze water
     */
    public boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultIceActions) return false;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        final Level world = user.getEntity().getLevel();
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.SNOW.defaultBlockState(), location);
        final BlockState state = context.getHitState();
        final Block block = state.getBlock();
        // // First attempt to freeze the water
        if (block == Blocks.WATER && state.getValue(LiquidBlock.LEVEL) == 0)
        {
            world.setBlockAndUpdate(context.getClickedPos(), Blocks.ICE.defaultBlockState());
            return true;
        }
        final InteractionResult result = context.getItemInHand().useOn(context);
        return result == InteractionResult.SUCCESS;
    }

    @Override
    public boolean isValid()
    {
        return move.getType(null) == PokeType.getType("ice") && move.isContact(null) && move.power > 0;
    }

}
