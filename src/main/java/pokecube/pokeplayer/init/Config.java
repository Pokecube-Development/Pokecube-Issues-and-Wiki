package pokecube.pokeplayer.init;

import pokecube.legends.Reference;
import thut.core.common.config.Configure;
import thut.core.common.config.Config.ConfigData;

public class Config extends ConfigData
{
    // Enabla PokePlayer
    @Configure(category = "general")
    public boolean enabledpokeplayer      = true;

    public boolean loaded = false;

    public Config()
    {
        super(Reference.ID);
    }

    @Override
    public void onUpdated()
    {
        if (!this.loaded) return;
        if (this.enabledpokeplayer)
        {
        }
    }
}