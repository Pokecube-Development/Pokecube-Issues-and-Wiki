package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.moves.MovesUtils;

public class Defiant extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.attacker == mob || move.pre || move.attacker == move.attacked) return;

        for(int i = 0; i < move.attackedStatModification.length; i++)
        {
            if(move.attackedStatModification[i] < 0) {
                MovesUtils.handleStats2(mob, mob.getOwner(), IMoveConstants.ATTACK, IMoveConstants.SHARP);
            }
        }
    }
}
