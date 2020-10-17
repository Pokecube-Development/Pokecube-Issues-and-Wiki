package pokecube.mobs.abilities.d;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;

public class Defiant extends Ability
{
  /*  @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.attacker == mob || move.pre || move.attacker == move.attacked) return;

        for(int i = 0; i < move.attackedStatModification.length; i++)
        {
            if(move.attackedStatModification[i] < 0) {
                MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.ATTACK, IMoveConstants.SHARP);
            }
        }
    }*/
}
