package pokecube.legends.init;

import net.minecraft.entity.Entity;
import pokecube.core.interfaces.IPokemob;

public class PokecubeDim
{
    public double beast(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        if (entity.dimension.getId() != 0) x = 3.7;
        return x;
    }
}
