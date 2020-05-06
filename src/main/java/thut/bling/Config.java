package thut.bling;

import net.minecraftforge.fml.config.ModConfig.Type;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    private static final String BAG = "bag";

    @Configure(category = Config.BAG, type = Type.SERVER)
    public int enderBagPages = 2;

    public Config()
    {
        super(ThutBling.MODID);
    }

    @Override
    public void onUpdated()
    {
    }

}
