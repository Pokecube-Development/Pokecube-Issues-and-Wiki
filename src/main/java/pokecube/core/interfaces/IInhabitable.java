package pokecube.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.core.handlers.events.SpawnHandler.ForbidRegion;

public interface IInhabitable
{

    void onExitHabitat(Mob mob);

    boolean onEnterHabitat(Mob mob);

    boolean canEnterHabitat(Mob mob);

    /**
     * Called when the habitat is broken due to the block being removed
     */
    default void onBroken(final ServerLevel world)
    {

    }

    /**
     * This may not be called for all types of this, only ones on custom tile
     * entities which tick it themselves will be called!
     */
    default void onTick(final ServerLevel world)
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

    default ForbidRegion getRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {
        return null;
    }

    default void updateRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {

    }
}
