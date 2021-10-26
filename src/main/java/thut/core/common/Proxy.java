package thut.core.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;

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

    default RegistryAccess getRegistries()
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
