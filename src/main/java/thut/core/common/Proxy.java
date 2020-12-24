package thut.core.common;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public interface Proxy
{

    default boolean isClientSide()
    {
        return false;
    }

    default boolean isServerSide()
    {
        return true;
    }

    default void loaded(final FMLLoadCompleteEvent event)
    {
    }

    default void setup(final FMLCommonSetupEvent event)
    {

    }

    default void setupClient(final FMLClientSetupEvent event)
    {
    }
}
