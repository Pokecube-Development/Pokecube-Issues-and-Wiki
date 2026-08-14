package pokecube.mobs.moves.attacks;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.api.moves.utils.MoveApplication.LastMoveEffect;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "kings-shield")
public class KingsShield implements PostMoveUse, LastMoveEffect
{
    @Override
    public void applyPostMove(Damage t) {
        MoveApplication packet = t.move();

        IPokemob attacker = packet.getUser();
        final IPokemob target = PokemobCaps.getPokemobFor(packet.getTarget());

        if (packet.canceled || packet.failed || target == null) return;

    }

    @Override
    public void applyLastMoveEffect(MoveApplication lastMove, MoveApplication nextMoveTarget)
    {
        PokecubeAPI.logInfo("applyLastMoveEffect(): move " + lastMove.getName() + " move used after " + nextMoveTarget.getName());
        IPokemob target = nextMoveTarget.getUser();
        // Return if the move used after King's shield did not fail or if no move was used after King's Shield.
        if (lastMove == null || nextMoveTarget == null || !nextMoveTarget.failed) return;
        if (nextMoveTarget.getMove().isContact(target)) // Lower attack if move made contact.
            MovesUtils.handleStats2(target, target.getOwner(), IMoveConstants.ATTACK,
                    IMoveConstants.FALL);
    }
}
