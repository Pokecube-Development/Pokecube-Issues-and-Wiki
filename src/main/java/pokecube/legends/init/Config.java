package pokecube.legends.init;

import net.minecraftforge.common.MinecraftForge;
import pokecube.legends.Reference;
import pokecube.legends.conditions.LegendaryConditions;
// import pokecube.legends.handlers.PortalSpawnHandler;
import pokecube.legends.handlers.WormHoleSpawnHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    // Enabla Condition
    @Configure(category = "general")
    public boolean enabledcondition      = true;
    @Configure(category = "general")
    public boolean singleUseLegendSpawns = false;
    @Configure(category = "general")
    public int     respawnLegendDelay    = 36000;

    @Configure(category = "raids")
    public int raidDuration = 3000;

    // mirage spot
    @Configure(category = "mirage")
    public boolean enabledmirage       = true;
    @Configure(category = "mirage")
    public double  mirageRespawnChance = 0.01;

    // ultra space portal
    @Configure(category = "ultraspace")
    public boolean enabledportal       = true;
    @Configure(category = "ultraspace")
    public int     ticksPerPortalSpawn = 9000;
    @Configure(category = "ultraspace")
    public int     portalDwellTime     = 9000;

    private final WormHoleSpawnHandler wormholes       = new WormHoleSpawnHandler();
    private boolean                    wormholeReged   = false;
    private final LegendaryConditions  conditions      = new LegendaryConditions();
    private boolean                    conditionsReged = false;

    public boolean loaded = false;

    public Config()
    {
        super(Reference.ID);
    }

    @Override
    public void onUpdated()
    {
        if (!this.loaded) return;
        if (this.enabledportal && !this.wormholeReged)
        {
            MinecraftForge.EVENT_BUS.register(this.wormholes);
            this.wormholeReged = true;
        }
        else if (!this.enabledportal && this.wormholeReged)
        {
            MinecraftForge.EVENT_BUS.unregister(this.wormholes);
            this.wormholeReged = false;
        }

        if (this.enabledcondition && !this.conditionsReged)
        {
            this.conditions.init();
            this.conditionsReged = true;
        }
    }

}
