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

    default Entity getTrackable(Entity input)
    {
        var entity = getEntity(input.level(), input.getId(), false);
        if (entity == null)
        {
            System.err.println("Trying to send a packet when something isn't tracked?");
            Thread.dumpStack();
        }
        return entity != null ? entity : input;
    }
}
