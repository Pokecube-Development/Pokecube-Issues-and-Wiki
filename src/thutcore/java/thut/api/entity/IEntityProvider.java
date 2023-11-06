package thut.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityProvider
{
    Entity getEntity(Level world, int id, boolean targetCopyMob);

    default Entity getEntity(Level world, int id)
    {
        return getEntity(world, id, true);
    }
}
