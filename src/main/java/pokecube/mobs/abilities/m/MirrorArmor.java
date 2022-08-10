package pokecube.mobs.abilities.m;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

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
