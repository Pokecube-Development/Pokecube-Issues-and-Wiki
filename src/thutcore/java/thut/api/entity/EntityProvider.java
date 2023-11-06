package thut.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thut.api.ThutCaps;

public class EntityProvider implements IEntityProvider
{
    // Provider for entities.
    public static IEntityProvider provider = new EntityProvider(null);

    final EntityProvider defaults;

    public EntityProvider(final EntityProvider defaults)
    {
        this.defaults = defaults;
    }

    @Override
    public Entity getEntity(final Level world, int id, final boolean targetCopyMob)
    {
        // In this case, we want to find for a copied mob instead!
        if (id <= -100)
        {
            id = -(id + 100);
            final Entity ret = world.getEntity(id);
            if (ret == null || !targetCopyMob)
            {
                if (ret == null && this.defaults != null) return this.defaults.getEntity(world, id, targetCopyMob);
                return ret;
            }
            final ICopyMob copy = ThutCaps.getCopyMob(ret);
            if (copy != null) return copy.getCopiedMob();
            return ret;
        }
        final Entity ret = world.getEntity(id);
        if (ret == null && this.defaults != null) return this.defaults.getEntity(world, id, targetCopyMob);
        return ret;
    }

}
