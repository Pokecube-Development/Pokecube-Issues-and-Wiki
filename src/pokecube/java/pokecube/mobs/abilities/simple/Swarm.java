package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@AbilityProvider(name = "swarm")
public class Swarm extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (mob != move.getUser()) return;
        if (move.type == PokeType.getType("bug") && mob.getEntity().getHealth() < mob.getEntity().getMaxHealth() / 3)
            move.pwr *= 1.5;
    }
}
