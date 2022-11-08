package thut.tech.common.handlers;

import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class ConfigHandler extends ConfigData
{

    @Configure(category = "speed")
    public double LiftSpeedUp = 0.3;
    @Configure(category = "speed")
    public double LiftSpeedDown = 0.35;
    @Configure(category = "speed")
    public double LiftAcceleration = 0.025;

    @Configure(category = "speed")
    public double LiftSpeedDownOccupied = 0;

    @Configure(category = "speed")
    public double LiftSpeedSideways = 0.5;

    @Configure(category = "controller")
    public int controllerProduction = 16;
    @Configure(category = "size")
    public int maxHeight = 5;
    @Configure(category = "size")
    public int maxRadius = 2;
    @Configure(category = "controller")
    public int maxLiftEnergy = 5000000;

    /**
     * @param MODID
     */
    public ConfigHandler(final String MODID)
    {
        super(MODID);
    }

    @Override
    public void onUpdated()
    {}

}
