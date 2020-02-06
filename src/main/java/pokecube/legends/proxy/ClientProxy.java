package pokecube.legends.proxy;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        // TODO is this still needed?
        // OBJLoader.INSTANCE..addDomain(Reference.ID);
    }
}
