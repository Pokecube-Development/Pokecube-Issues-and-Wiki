package pokecube.mobs.moves.attacks.ongoing;

import java.util.Random;

import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.utils.PokeType;

public class Move_Firespin extends Move_Ongoing
{

    public Move_Firespin()
    {
        super("firespin");
    }

    @Override
    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob.getEntity());
        if (pokemob != null && pokemob.isType(PokeType.getType("ghost"))) return;
        super.doOngoingEffect(mob, effect);
    }

    @Override
    public int getDuration()
    {
        final Random r = new Random();
        return 2 + r.nextInt(4);
    }

}
