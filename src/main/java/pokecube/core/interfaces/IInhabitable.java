package pokecube.core.interfaces;

import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

public interface IInhabitable
{

    void onExitHabitat(MobEntity mob);

    boolean onEnterHabitat(MobEntity mob);

    boolean canEnterHabitat(MobEntity mob);

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
}
