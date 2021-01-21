package pokecube.core.world;

import net.minecraft.world.server.ServerWorld;

public interface IWorldTickListener
{
    default void onAttach(final ServerWorld world)
    {
    }

    default void onDetach(final ServerWorld world)
    {
    }

    default void onTickEnd(final ServerWorld world)
    {
    }

    default void onTickStart(final ServerWorld world)
    {
    }
}
