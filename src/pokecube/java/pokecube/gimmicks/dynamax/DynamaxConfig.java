package pokecube.gimmicks.dynamax;

import pokecube.gimmicks.terastal.TerastalRaid;
import thut.core.common.config.Config;
import thut.core.common.config.Configure;

public class DynamaxConfig extends Config.ConfigData
{
    public static final String dynamax = "dynamax";

    @Configure(category = dynamax, comment = "Dynamax cooldown in ticks. [Default: 6000]")
    public int dynamax_cooldown = 6000;
    @Configure(category = dynamax, comment = "Dynamax duration in ticks. [Default: 250]")
    public int dynamax_duration = 250;
    @Configure(category = dynamax, comment = "Scale of dynamaxed pokemobs. [Default: 5.0]")
    public double dynamax_scale = 5.0;
    @Configure(category = dynamax, comment = "Z-Move cooldown in ticks. [Default: 2000]")
    public int z_move_cooldown = 2000;
    @Configure(category = "raids", comment = "Duration of raids in ticks. [Default: 3000]")
    public int raidDuration = 3000;

    public DynamaxConfig()
    {
        super(dynamax);
    }

    @Override
    protected void onUpdated()
    {
        DynamaxRaid.RAID_DURATION = raidDuration;
        TerastalRaid.RAID_DURATION = raidDuration;
    }
}
