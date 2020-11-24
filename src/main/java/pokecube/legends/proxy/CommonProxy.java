package pokecube.legends.proxy;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.legends.init.DimensionInit;
import pokecube.legends.init.PlantsInit;
import pokecube.legends.worldgen.StructuresDimension;
import pokecube.legends.worldgen.dimension.DistortionWorldConfig;
import pokecube.legends.worldgen.dimension.UltraSpaceConfig;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
    	new UltraSpaceConfig().init(event);
        new DistortionWorldConfig().init(event);
        new PlantsInit().init(event);
        new StructuresDimension().SpawnInit(event);
    }
}
