package pokecube.mobs.abilities.c;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.IMoveConstants;
import pokecube.core.database.abilities.Ability;
import pokecube.core.moves.MovesUtils;

public class CottonDown extends Ability
{
	@Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
		final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit) MovesUtils.handleStats2(mob, attacker.getEntity(),
        		IMoveConstants.VIT, IMoveConstants.FALL);
    }
}
