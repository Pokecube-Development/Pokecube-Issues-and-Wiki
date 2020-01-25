package pokecube.legends.proxy;

import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.Reference;

public class ClientProxy extends CommonProxy
{

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        OBJLoader.INSTANCE.addDomain(Reference.ID);
    }
}
