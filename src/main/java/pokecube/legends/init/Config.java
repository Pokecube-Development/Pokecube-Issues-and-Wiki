package pokecube.legends.init;

import net.minecraftforge.common.MinecraftForge;
import pokecube.legends.Reference;
import pokecube.legends.conditions.LegendaryConditions;
//import pokecube.legends.handlers.PortalSpawnHandler;
import pokecube.legends.handlers.WormHoleSpawnHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    // Enabla Condition
    @Configure(category = "general")
    public boolean enabledcondition   = true;
    @Configure(category = "general")
    public int     respawnLegendDelay = 36000;

    // mirage spot
    @Configure(category = "mirage")
    public boolean enabledmirage       = true;
    @Configure(category = "mirage")
    public int     ticksPerMirageSpawn = 7000;

    // ultra space portal
    @Configure(category = "ultraspace")
    public boolean enabledportal       = true;
    @Configure(category = "ultraspace")
    public int     ticksPerPortalSpawn = 9000;

    //private final PortalSpawnHandler   portals         = new PortalSpawnHandler();
    private boolean                    portalReged     = false;
    private final WormHoleSpawnHandler wormholes       = new WormHoleSpawnHandler();
    private boolean                    wormholeReged   = false;
    private final LegendaryConditions  conditions      = new LegendaryConditions();
    private boolean                    conditionsReged = false;

    public Config()
    {
        super(Reference.ID);
    }

    @Override
    public void onUpdated()
    {
        //if (this.enabledmirage && !this.portalReged)
        //{
        //    MinecraftForge.EVENT_BUS.register(this.portals);
        //    this.portalReged = true;
        //}
        //else if (!this.enabledmirage && this.portalReged)
        //{
        //    MinecraftForge.EVENT_BUS.unregister(this.portals);
        //    this.portalReged = false;
        //}

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
