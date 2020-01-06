package pokecube.core.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IEntityProvider
{
    Entity getEntity(World world, int id, boolean expectsPokemob);
}
