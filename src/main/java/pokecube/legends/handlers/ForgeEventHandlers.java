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
    	//Ultra Space
        ModDimensions.DIMENSION_TYPE_US = DimensionManager.registerOrGetDimension(ModDimensions.DIMENSION_ULTRASPACE,
                ModDimensions.DIMENSION_U, null, false);
        if (ModDimensions.DIMENSION_TYPE_US.getRegistryName() == null) ModDimensions.DIMENSION_TYPE_US.setRegistryName(
                ModDimensions.DIMENSION_ULTRASPACE);
        
        //Distortic World
        ModDimensions.DIMENSION_TYPE_DW = DimensionManager.registerOrGetDimension(ModDimensions.DIMENSION_DISTORTIC,
                ModDimensions.DIMENSION_D, null, false);
        if (ModDimensions.DIMENSION_TYPE_DW.getRegistryName() == null) ModDimensions.DIMENSION_TYPE_DW.setRegistryName(
                ModDimensions.DIMENSION_DISTORTIC);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void capabilityEntities(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(EventsHandler.POKEMOBCAP)) event.addCapability(
                ForgeEventHandlers.ZMOVECAP, new ZPowerHandler());
    }
}
