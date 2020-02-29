package pokecube.legends.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class ForgeEventHandlers
{
    private static final ResourceLocation ZMOVECAP = new ResourceLocation("pokecube_legends:zmove_check");

    @SubscribeEvent
    public void onDimensionRegistry(final RegisterDimensionsEvent event)
    {
        ModDimensions.DIMENSION_TYPE = DimensionManager.registerOrGetDimension(ModDimensions.DIMENSION_ID,
                ModDimensions.DIMENSION, null, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)) event.addCapability(
                ForgeEventHandlers.ZMOVECAP, new ZPowerHandler());
    }
}
