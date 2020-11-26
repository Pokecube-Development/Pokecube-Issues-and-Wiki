package pokecube.legends.init;

import pokecube.legends.Reference;
import pokecube.legends.conditions.LegendaryConditions;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    // Enabla Condition Legendary
    @Configure(category = "general")
    public boolean enabledcondition      = true;
    @Configure(category = "general")
    public boolean singleUseLegendSpawns = false;
    @Configure(category = "general")
    public int     respawnLegendDelay    = 36000;

    // Meteor adjustments
    @Configure(category = "meteors")
    public double meteorPowerThreshold = 20;
    @Configure(category = "meteors")
    public double meteorChanceForAny = 0.01;
    @Configure(category = "meteors")
    public double meteorChanceForDust = 0.25;


    //Raids
    @Configure(category = "raids")
    public int raidDuration = 3000;

    //Mirage Spot(Hoppa Ring)
    @Configure(category = "mirage")
    public boolean enabledmirage       = true;
    @Configure(category = "mirage")
    public double  mirageRespawnChance = 0.01;

    //Ultra Space
    @Configure(category = "ultraspace")
    public boolean enabledkeyusecombustible    	= true;
    @Configure(category = "ultraspace")
    public int     itemCombustiveStack     		= 5;

    //Distortic World
    @Configure(category = "distortic")
    public int     mirrorCooldown     	= 800;

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
        if (this.enabledcondition && !this.conditionsReged)
        {
            this.conditions.init();
            this.conditionsReged = true;
        }

        if(this.enabledkeyusecombustible == true) if(this.itemCombustiveStack <= 1 || this.itemCombustiveStack >= 10)
        	this.itemCombustiveStack = 5;

       if(this.mirrorCooldown <= 300 || this.mirrorCooldown >= 2000) this.mirrorCooldown = 800;
    }
}
