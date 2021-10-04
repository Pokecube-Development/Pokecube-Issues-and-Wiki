package pokecube.core.entity.pokemobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;

/**
 * This class is copied by ByteClassLoader to make seperate classes for each
 * pokemob
 */
public class GenericPokemob extends EntityPokemob
{
    public GenericPokemob(final EntityType<? extends ShoulderRidingEntity> type, final Level world)
    {
        super(type, world);
    }
}
