package pokecube.api.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IEntityProvider
{
    Entity getEntity(Level world, int id, boolean expectsPokemob);
}
