package pokecube.core.moves.world;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import thut.api.maths.Vector3;

public class DefaultAction implements IMoveWorldEffect
{
    MoveEntry move;

    public DefaultAction(final MoveEntry move)
    {
        this.move = move;
    }

    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        if (!PokecubeCore.getConfig().defaultInteractAction) return false;
        var player = MoveEventsHandler.getRelevantPlayer(user);
        final MoveEventsHandler.UseContext context = MoveEventsHandler.getContext(player.level, user,
                Blocks.TORCH.defaultBlockState(), location);
        var event = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, location.getPos(),
                context.getBlockHitResult());
        if (!event.isCanceled())
        {
            var state = context.getHitState();
            var result = state.useWithoutItem(player.level(), player, context.getBlockHitResult());
            if(result.indicateItemUse()) return true;
            return true;
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return this.move.name;
    }

    @Override
    public boolean isValid()
    {
        return move.power > 0;
    }
}
