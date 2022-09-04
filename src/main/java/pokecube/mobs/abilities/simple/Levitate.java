package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.utils.PokeType;

public class Levitate extends Ability
{
    @Override
    public int beforeDamage(final IPokemob mob, final MovePacket move, final int damage)
    {
        boolean weAreTarget = mob.getEntity() == move.attacked && mob.getAbility() == this;
        if (weAreTarget && move.getMove().getType(move.attacker) == PokeType.getType("ground") && move.attacked == mob)
            return 0;
        return super.beforeDamage(mob, move, damage);
    }

    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        if (!move.pre) return;
        boolean weAreTarget = mob.getEntity() == move.attacked && mob.getAbility() == this;
        if (weAreTarget && move.getMove().getType(move.attacker) == PokeType.getType("ground")) move.canceled = true;
    }
}
