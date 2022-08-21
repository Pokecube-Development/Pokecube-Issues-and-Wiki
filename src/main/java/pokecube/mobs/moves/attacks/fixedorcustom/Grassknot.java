package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.Entity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.moves.templates.Move_Basic;

public class Grassknot extends Move_Basic
{

    public Grassknot()
    {
        super("grassknot");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final int pwr = 120;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return pwr;
        final double mass = targetMob.getWeight();
        if (mass < 10) return 20;
        if (mass < 25) return 40;
        if (mass < 50) return 60;
        if (mass < 100) return 80;
        if (mass < 200) return 100;

        return pwr;
    }
}
