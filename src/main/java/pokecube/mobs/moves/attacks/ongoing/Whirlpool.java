package pokecube.mobs.moves.attacks.ongoing;

import java.util.Random;

import net.minecraft.entity.LivingEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;
import pokecube.core.utils.PokeType;

public class Whirlpool extends Move_Ongoing
{

    public Whirlpool()
    {
        super("whirlpool");
    }

    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob.getEntity());
        if (pokemob != null && pokemob.isType(PokeType.getType("ghost"))) return;
        super.doOngoingEffect(user, mob, effect);
    }

    @Override
    public int getDuration()
    {
        final Random r = new Random();
        return 2 + r.nextInt(4);
    }

}
