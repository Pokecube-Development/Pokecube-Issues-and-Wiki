package pokecube.core.utils.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public interface IBehaviourAccessor<E extends LivingEntity>
{
    default boolean invokeCanStillUse(ServerLevel level, E entity, long gameTime) {return false;}
}
