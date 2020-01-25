package pokecube.legends.init;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PokecubeDim
{
    public double beast(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        if (entity.dimension.getId() != 0) x = 3.7;
        return x;
    }

    // Dynamax
    public double dynamax(IPokemob mob)
    {
        double x = 0.1;
        final MobEntity entity = mob.getEntity();
        mob = CapabilityPokemob.getPokemobFor(entity);
        if (entity != null)
        {
            mob.setSize(1f);
            mob.setHealth(entity.getMaxHealth());
            x = 60;
            // System.console().printf("ta aki");
        }
        return x;
    }
}
