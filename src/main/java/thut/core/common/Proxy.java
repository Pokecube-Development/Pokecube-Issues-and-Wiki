package thut.core.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public interface Proxy
{

    default boolean isClientSide()
    {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    default boolean isServerSide()
    {
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    default DynamicRegistries getRegistries()
    {
        try
        {
            final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            return server.registryAccess();
        }
        catch (final Exception e)
        {
            // During pre-loading or similar, so exit.
            return null;
        }
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
