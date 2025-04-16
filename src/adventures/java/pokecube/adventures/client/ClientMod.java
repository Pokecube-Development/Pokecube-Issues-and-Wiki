package pokecube.adventures.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import pokecube.adventures.PokecubeAdv;

@Mod(value = PokecubeAdv.MODID, dist = Dist.CLIENT)
public class ClientMod
{
    public ClientMod(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));
    }
}
