package pokecube.adventures;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.config.ModConfig.Type;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.utils.EnergyHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final Config instance = new Config();

    private static final String MACHINE = "machine";
    private static final String TRAINER = "trainers";

    public List<Class<? extends LivingEntity>> customTrainers = Lists.newArrayList();

    @Configure(category = Config.TRAINER)
    public boolean npcsAreTrainers         = true;
    @Configure(category = Config.TRAINER)
    public int     trainerCooldown         = 5000;
    @Configure(category = Config.TRAINER)
    public int     trainerSightRange       = 8;
    @Configure(category = Config.TRAINER)
    public int     trainerBattleDelay      = 50;
    @Configure(category = Config.TRAINER)
    public int     trainerSendOutDelay     = 50;
    @Configure(category = Config.TRAINER)
    public boolean trainerslevel           = true;
    @Configure(category = Config.TRAINER)
    public boolean trainerSpawn            = true;
    @Configure(category = Config.TRAINER)
    public int     trainerBox              = 64;
    @Configure(category = Config.TRAINER)
    public double  trainerDensity          = 2;
    @Configure(category = Config.TRAINER)
    public boolean pokemobsHarmNPCs        = false;
    @Configure(category = Config.TRAINER)
    public boolean trainersBattleEachOther = true;
    @Configure(category = Config.TRAINER)
    public boolean trainersBattlePokemobs  = true;
    @Configure(category = Config.TRAINER)
    public int     trainerDeAgressTicks    = 100;
    @Configure(category = Config.TRAINER)
    public boolean trainersMate            = true;
    @Configure(category = Config.TRAINER)
    public String  defaultReward           = "minecraft:emerald";

    @Configure(type = Type.CLIENT, category = Config.MACHINE)
    public boolean expandedDNATooltips      = false;
    @Configure(category = Config.MACHINE)
    public int     fossilReanimateCost      = 50000;
    @Configure(category = Config.MACHINE)
    public boolean anyReanimate             = true;
    @Configure(category = Config.MACHINE)
    public int     maxOutput                = 256;
    @Configure(category = Config.MACHINE)
    public int     energyHungerCost         = 5;
    @Configure(category = Config.MACHINE)
    public String  powerFunction            = "a*x/10";
    @Configure(category = Config.MACHINE)
    public String  warpPadCostFunction      = "(dx)*(dx) + (dy)*(dy) + (dz)*(dz) + (5*dw)^4";
    @Configure(category = Config.MACHINE)
    public String  clonerEfficiencyFunction = "x";

    public Config()
    {
        super(PokecubeAdv.ID);
    }

    @Override
    public void onUpdated()
    {
        EnergyHandler.initParser();
        BaseGeneticsTile.initParser(this.clonerEfficiencyFunction);
    }

}
