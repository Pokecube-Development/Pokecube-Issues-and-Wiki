package pokecube.core.entity.pokemobs;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.world.World;

/**
 * This class is copied by ByteClassLoader to make seperate classes for each
 * pokemob
 */
public class GenericPokemob extends EntityPokemob
{
    public GenericPokemob(final EntityType<? extends ShoulderRidingEntity> type, final World world)
    {
        super(type, world);
    }
}
