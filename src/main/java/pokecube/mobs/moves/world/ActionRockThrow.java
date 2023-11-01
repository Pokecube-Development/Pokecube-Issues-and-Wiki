package pokecube.mobs.moves.world;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class ActionRockThrow implements IMoveWorldEffect
{

    public ActionRockThrow()
    {}

    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, getMoveName())) return false;

        final Level world = user.getEntity().getLevel();
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.COBBLESTONE.defaultBlockState(),
                location);
        final InteractionResult result = context.getItemInHand().useOn(context);
        return result == InteractionResult.SUCCESS;
    }

    @Override
    public String getMoveName()
    {
        return "rock-throw";
    }

}
