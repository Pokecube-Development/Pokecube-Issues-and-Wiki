package pokecube.legends.handlers;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class ForgeEventHandlers {
	
	@SubscribeEvent
	public void onDimensionRegistry(RegisterDimensionsEvent event) {
		ModDimensions.DIMENSION_TYPE = DimensionManager.registerOrGetDimension(ModDimensions.DIMENSION_ID,
				ModDimensions.DIMENSION, null, true);
	}
}
