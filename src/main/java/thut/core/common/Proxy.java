package thut.core.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public interface Proxy
{
    /**
     * On client side, this returns the player.
     *
     * @return
     */
    default PlayerEntity getPlayer()
    {
        return null;
    }

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
