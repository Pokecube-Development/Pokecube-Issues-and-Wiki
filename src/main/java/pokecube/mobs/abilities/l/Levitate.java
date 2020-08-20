package pokecube.mobs.abilities.l;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.PokeType;

public class Levitate extends Ability
{
    @Override
    public int beforeDamage(final IPokemob mob, final MovePacket move, final int damage)
    {
        if (move.getMove().getType(move.attacker) == PokeType.getType("ground") && move.attacked == mob) return 0;
        return super.beforeDamage(mob, move, damage);
    }

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (move.attacker == mob || !move.pre || move.attacker == move.attacked) return;
        if (move.getMove().getType(move.attacker) == PokeType.getType("ground")) move.canceled = true;
    }
}
