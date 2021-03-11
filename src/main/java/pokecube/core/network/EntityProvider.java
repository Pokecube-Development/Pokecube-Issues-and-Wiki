package pokecube.core.network;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import pokecube.core.interfaces.IEntityProvider;

public class EntityProvider implements IEntityProvider
{
    final EntityProvider defaults;

    public EntityProvider(EntityProvider defaults)
    {
        this.defaults = defaults;
    }

    @Override
    public Entity getEntity(World world, int id, boolean expectsPokemob)
    {
        final Entity ret = world.getEntity(id);
        if (ret == null && this.defaults != null) return this.defaults.getEntity(world, id, expectsPokemob);
        return ret;
    }

}
