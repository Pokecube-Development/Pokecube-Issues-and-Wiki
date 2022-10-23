package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.Move_Base;

public class Libero extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final Move_Base attack = move.getMove();
        if (!move.pre || move.attack.equals("struggle")) return;
        if (mob == move.attacker) mob.setType1(attack.move.type);
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (!mob.inCombat()) mob.setType1(null);
    }

    @Override
    public IPokemob onRecall(final IPokemob mob)
    {
        mob.setType1(null);
        return super.onRecall(mob);
    }
}
