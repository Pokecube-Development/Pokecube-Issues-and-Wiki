package pokecube.gimmicks.item_taming;

import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class TamingConfig extends ConfigData
{
    @Configure(comment = "Setting this to true will disable this gimmick entirely", gameRestart = true)
    public boolean itemTamingDisabled = false;

    public TamingConfig()
    {
        super("item_taming");
    }

    @Override
    protected void onUpdated()
    {
    }
}
