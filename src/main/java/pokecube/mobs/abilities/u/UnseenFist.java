package pokecube.mobs.abilities.u;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.abilities.Ability;

public class UnseenFist extends Ability
{
	@Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        final Move_Base attack = move.getMove();
        
        if (move.pre) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT) {
        	move.failed = false;
        }
    }
}
