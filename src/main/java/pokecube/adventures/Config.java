package pokecube.adventures;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.config.ModConfig.Type;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final Config instance = new Config();

    @Configure(type = Type.CLIENT)
    public boolean expandedDNATooltips = false;

    public List<Class<? extends LivingEntity>> customTrainers = Lists.newArrayList();

    @Configure
    public boolean npcsAreTrainers = true;

    @Configure
    public int trainerCooldown = 5000;

    @Configure
    public int trainerSightRange = 8;

    @Configure
    public int trainerBattleDelay = 5000;

    @Configure
    public int trainerSendOutDelay = 50;

    @Configure
    public boolean trainerslevel = true;

    @Configure
    public boolean trainerSpawn = true;

    @Configure
    public int trainerBox = 64;

    @Configure
    public double trainerDensity = 2;

    @Configure
    public boolean pokemobsHarmNPCs = false;

    @Configure
    public boolean trainersBattleEachOther = true;

    @Configure
    public boolean trainersBattlePokemobs = true;

    @Configure
    public int trainerDeAgressTicks = 100;

    @Configure
    public boolean trainersMate = true;

    @Configure
    public String defaultReward = "minecraft:emerald";

    @Configure
    public int fossilReanimateCost = 50000;

    @Configure
    public boolean anyReanimate = true;

    public Config()
    {
        super(PokecubeAdv.ID);
    }

    @Override
    public void onUpdated()
    {
        // TODO Auto-generated method stub

    }

}
