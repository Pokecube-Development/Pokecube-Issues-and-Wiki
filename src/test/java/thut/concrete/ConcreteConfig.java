package thut.concrete;

import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class ConcreteConfig extends ConfigData
{

    @Configure(category = "Volcanoes", comment = "Are volcanoes active, set to false to prevent them from growing. [Default: true]")
    public boolean volcanoes_tick = true;

    /**
     * @param MODID
     */
    public ConcreteConfig(final String MODID)
    {
        super(MODID);
    }

    @Override
    public void onUpdated()
    {}

}
