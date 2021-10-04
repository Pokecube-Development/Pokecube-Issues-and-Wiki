package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.Entity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class Gyroball extends Move_Basic
{

    public Gyroball()
    {
        super("gyroball");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null) return 50;
        final int targetSpeed = targetMob.getStat(Stats.VIT, true);
        final int userSpeed = user.getStat(Stats.VIT, true);
        final int pwr = 25 * targetSpeed / userSpeed;
        return pwr;
    }
}
