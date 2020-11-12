package pokecube.legends.proxy;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.legends.init.DimensionInit;
import pokecube.legends.init.PlantsInit;
import pokecube.legends.worldgen.dimension.UltraSpaceRenderInfo;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{
    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
    	//DimensionRenderInfo.FogType(DimensionInit.ULTRASPACE_TYPE.getLocation(), new UltraSpaceRenderInfo());
        //new PlantsInit().init(event);
    }
}
