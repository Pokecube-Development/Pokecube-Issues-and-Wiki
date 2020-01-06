package pokecube.core.moves.zmoves;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;

public class ZMoveManager
{
    /**
     * Returns a Z move based on the current selected attack of the user, if no
     * zmove available, or not able to use one (ie no crystal held), or the mob
     * is on cooldown for zmoves, then this will return null.
     *
     * @param user
     * @return
     */
    public static Move_Base getZMove(IPokemob user)
    {
        if (user.getCombatState(CombatStates.USEDZMOVE)) return null;
        final int selected = user.getMoveIndex();
        if (selected >= user.getMoves().length) return null;
        final Move_Base move = MovesUtils.getMoveFromName(user.getMoves()[selected]);
        if (move == null) return null;
        final String zMove = move.move.baseEntry.zMovesTo;
        return MovesUtils.getMoveFromName(zMove);
    }
}
