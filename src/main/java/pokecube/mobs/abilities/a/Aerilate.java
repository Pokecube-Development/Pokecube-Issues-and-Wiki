package pokecube.mobs.abilities.a;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;

/**
 *   Wiki description
 *   Aerilate causes all Normal-type moves used by the Pokémon to become
 *   Flying-type and receive a 20% power boost. It overrides Ion Deluge and Electrify.
 *   If Max Strike is selected with Aerilate, it'll turn into Max Airstream when used.
 **/

public class Aerilate extends Ability
{
    @Override
    public void preMove(IPokemob mob, MovePacket move)
    {
        if (move.attackType == PokeType.getType("normal") && mob == move.attacker)
        {
            if("maxstrike".equals(move.attack)) {
                Move_Base newMove = MovesUtils.getMoveFromName("maxairstream");
                move.changeMove(newMove);
            }

            move.attackType = PokeType.getType("flying");
            move.PWR *= 1.2;
        }
    }
}
