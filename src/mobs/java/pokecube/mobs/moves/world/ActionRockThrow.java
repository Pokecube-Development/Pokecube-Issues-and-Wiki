package pokecube.mobs.moves.world;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import pokecube.core.moves.world.DefaultAction;
import thut.api.maths.Vector3;

public class ActionRockThrow implements IMoveWorldEffect
{

    public ActionRockThrow()
    {}

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location, HitResult hit)
    {
        if(!(hit instanceof BlockHitResult blockHit)) return false;
        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, getMoveName())) return false;
        final Level world = user.getEntity().level();
        Direction direction = DefaultAction.getHitDirection(world, blockHit);
        var newHit = blockHit.withDirection(direction);
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.COBBLESTONE.defaultBlockState(), newHit);
        final InteractionResult result = context.getItemInHand().useOn(context);
        return result == InteractionResult.SUCCESS;
    }

    @Override
    public String getMoveName()
    {
        return "rock-throw";
    }

}
