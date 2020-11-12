package pokecube.legends.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.legends.init.DimensionInit;

public class ForgeEventHandlers
{
    private static final ResourceLocation ZMOVECAP = new ResourceLocation("pokecube_legends:zmove_check");

    /*@SubscribeEvent
    public void onDimensionRegistry(final RegisterDimensionsEvent event)
    {
        DimensionInit.DIMENSION_TYPE = DimensionManager.registerOrGetDimension(DimensionInit.DIMENSION_ID,
                DimensionInit.DIMENSION, null, false);
        if (DimensionInit.DIMENSION_TYPE.getRegistryName() == null) DimensionInit.DIMENSION_TYPE.setRegistryName(
                DimensionInit.DIMENSION_ID);
    }*/

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)) event.addCapability(
                ForgeEventHandlers.ZMOVECAP, new ZPowerHandler());
    }
}
