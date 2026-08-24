package pokecube.gimmicks.nests;

import thut.core.common.config.Config;
import thut.core.common.config.Configure;

public class NestConfig extends Config.ConfigData
{
    public static final String nests = "nests";
    
    @Configure(comment = "Wild pokemobs make nests, these result in effective mob spawners where they made them, and will prevent other mobs spawning in the area, unless the nests are cleared out. [Default: true]")
    public boolean pokemobsMakeNests = true;
    @Configure(comment = "Probability per second of a nest spawning an egg, if it has less than 3 eggs. [Default: 0.25]")
    public double nestEggRate = 0.25;
    @Configure(comment = "The number of pokemobs that work at one nest. [Default: 3]")
    public int nestMobNumber = 3;
    @Configure(comment = "The number of ant pokemobs that work at one nest. [Default: 10]")
    public int antNestMobNumber = 10;
    @Configure(comment = "Minimum distance between burrows made by wild pokemobs. [Default: 64]")
    public int nestSpacing = 64;

    public NestConfig()
    {
        super(nests);
    }

    @Override
    protected void onUpdated()
    {
    }
}
