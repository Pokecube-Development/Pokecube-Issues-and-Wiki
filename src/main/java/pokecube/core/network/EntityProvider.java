package pokecube.core.network;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import pokecube.core.interfaces.IEntityProvider;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class EntityProvider implements IEntityProvider
{
    final EntityProvider defaults;

    public EntityProvider(final EntityProvider defaults)
    {
        this.defaults = defaults;
    }

    @Override
    public Entity getEntity(final World world, int id, final boolean expectsPokemob)
    {
        // In this case, we want to find for a copied mob instead!
        if (id <= -100)
        {
            id = -(id + 100);
            final Entity ret = world.getEntity(id);
            if (ret == null || !expectsPokemob)
            {
                if (ret == null && this.defaults != null) return this.defaults.getEntity(world, id, expectsPokemob);
                return ret;
            }
            final ICopyMob copy = CopyCaps.get(ret);
            if (copy != null) return copy.getCopiedMob();
            return ret;
        }
        final Entity ret = world.getEntity(id);
        if (ret == null && this.defaults != null) return this.defaults.getEntity(world, id, expectsPokemob);
        return ret;
    }

}
