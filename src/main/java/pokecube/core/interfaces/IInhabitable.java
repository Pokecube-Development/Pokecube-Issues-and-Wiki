package pokecube.core.interfaces;

import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.handlers.events.SpawnHandler.ForbidRegion;

public interface IInhabitable
{

    void onExitHabitat(MobEntity mob);

    boolean onEnterHabitat(MobEntity mob);

    boolean canEnterHabitat(MobEntity mob);

    /**
     * Called when the habitat is broken due to the block being removed
     */
    default void onBroken(final ServerWorld world)
    {

    }

    /**
     * This may not be called for all types of this, only ones on custom tile
     * entities which tick it themselves will be called!
     */
    default void onTick(final ServerWorld world)
    {

    }

    /**
     * If this is a savable habitat, this should return a unique key, otherwise
     * it can stay null.
     *
     * @return
     */
    default ResourceLocation getKey()
    {
        return null;
    }

    default void setPos(final BlockPos pos)
    {

    }

    default ForbidRegion getRepelledRegion()
    {
        return null;
    }

    default void updateRepelledRegion()
    {

    }
}
