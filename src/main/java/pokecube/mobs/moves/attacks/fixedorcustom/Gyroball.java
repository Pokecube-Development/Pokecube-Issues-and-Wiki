package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.moves.templates.Move_Basic;

public class Gyroball extends Move_Basic
{

    public Gyroball()
    {
        super("gyroball");
    }

    @Override
    public int getPWR(IPokemob user, LivingEntity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return 50;
        final int targetSpeed = targetMob.getStat(Stats.VIT, true);
        final int userSpeed = user.getStat(Stats.VIT, true);
        final int pwr = 25 * targetSpeed / userSpeed;
        return pwr;
    }
}
