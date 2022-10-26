package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;

@AbilityProvider(name = "overgrow")
public class OverGrow extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        if (move.type == PokeType.getType("grass") && mob.getEntity().getHealth() < mob.getEntity().getMaxHealth() / 3)
            move.pwr *= 1.5;
    }
}
