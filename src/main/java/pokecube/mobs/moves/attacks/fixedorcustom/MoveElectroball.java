package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class MoveElectroball extends Move_Basic
{

    public MoveElectroball()
    {
        super("electroball");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null) return 50;
        final int targetSpeed = targetMob.getStat(Stats.VIT, true);
        final int userSpeed = user.getStat(Stats.VIT, true);
        int pwr = 60;
        final double var = (double) targetSpeed / (double) userSpeed;
        if (var < 0.25) pwr = 150;
        else if (var < 0.33) pwr = 120;
        else if (var < 0.5) pwr = 80;
        else pwr = 60;
        return pwr;
    }
}
