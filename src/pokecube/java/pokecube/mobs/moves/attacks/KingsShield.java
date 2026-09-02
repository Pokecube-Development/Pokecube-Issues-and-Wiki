package pokecube.mobs.moves.attacks;

import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;
import pokecube.api.moves.utils.MoveApplication.LastMoveEffect;
import pokecube.api.moves.utils.MoveApplication.BlockCondition;
import pokecube.core.moves.MovesUtils;

@MoveProvider(name = "kings-shield")
public class KingsShield implements PostMoveUse, LastMoveEffect, BlockCondition
{
    @Override
    public void applyPostMove(Damage t) {
        // This wasn't doing anything?
    }

    @Override
    public void applyLastMoveEffect(MoveApplication lastMove, MoveApplication nextMoveTarget)
    {
        IPokemob target = nextMoveTarget.getUser();
        // Return if the next move did not fail or if no move was used after King's Shield or we used the next move.
        if (lastMove == null || nextMoveTarget == null || !nextMoveTarget.failed || nextMoveTarget.getUser() == lastMove.getUser()) return;
        if (nextMoveTarget.getMove().isContact(target)) // Lower attack if move made contact.
            MovesUtils.handleStats2(target, lastMove.getUserEntity(), IMoveConstants.ATTACK,
                    IMoveConstants.FALL);
    }

    @Override
    // King's Shield only blocks physical and special moves.
    public boolean matches(IPokemob blocker, IPokemob user, MoveEntry incomingMove)
    {
        return incomingMove.category == IMoveConstants.AttackCategory.PHYSICAL || incomingMove.category == IMoveConstants.AttackCategory.SPECIAL;
    }
}
