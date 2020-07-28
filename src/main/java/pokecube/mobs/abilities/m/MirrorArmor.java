package pokecube.mobs.abilities.m;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class MirrorArmor extends Ability
{
	@Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
		final IPokemob attacker = move.attacker;
		
		if (move.pre || attacker == move.attacked) {
			return;
		}
		else
        {
            move.attackedStatModification = move.attackerStatModification;
            move.attackedStatModProb = move.attackerStatModProb;
        }
    }
}
