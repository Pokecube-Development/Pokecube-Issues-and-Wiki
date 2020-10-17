package pokecube.mobs.abilities.u;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class UnseenFist extends Ability
{
	/*@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();
        
        if (move.pre) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT) {
        	move.failed = false;
        }
    }*/
}
