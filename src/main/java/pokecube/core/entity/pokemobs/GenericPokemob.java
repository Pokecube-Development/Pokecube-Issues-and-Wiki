package pokecube.core.entity.pokemobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;

/**
 * This class is copied by ByteClassLoader to make seperate classes for each
 * pokemob
 */
public class GenericPokemob extends EntityPokemob
{
    public GenericPokemob(EntityType<? extends TameableEntity> type, World world)
    {
        super(type, world);
    }
}
