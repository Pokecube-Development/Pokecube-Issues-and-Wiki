package thut.api.world;

import net.minecraft.server.level.ServerLevel;

public interface IWorldTickListener
{
    default void onAttach(final ServerLevel world)
    {
    }

    default void onDetach(final ServerLevel world)
    {
    }

    default void onTickEnd(final ServerLevel world)
    {
    }

    default void onTickStart(final ServerLevel world)
    {
    }
}
