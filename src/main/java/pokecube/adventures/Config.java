package pokecube.adventures;

import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.config.ModConfig.Type;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.daycare.DaycareTile;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper.DNAPack;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.utils.EnergyHandler;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.items.ItemFossil;
import thut.api.entity.genetics.Alleles;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final Config instance = new Config();

    private static final String MACHINE = "machine";
    private static final String TRAINER = "trainers";
    private static final String BAG     = "bag";

    @Configure(category = Config.TRAINER)
    public boolean npcsAreTrainers = true;

    @Configure(category = Config.TRAINER)
    public int trainerCooldown     = 5000;
    @Configure(category = Config.TRAINER)
    public int trainerSightRange   = 8;
    @Configure(category = Config.TRAINER)
    public int trainerBattleDelay  = 50;
    @Configure(category = Config.TRAINER)
    public int trainerSendOutDelay = 50;
    @Configure(category = Config.TRAINER)
    public int trainerAgroRate     = 20;

    @Configure(category = Config.TRAINER)
    public boolean trainerslevel           = true;
    @Configure(category = Config.TRAINER)
    public boolean trainerSpawn            = true;
    @Configure(category = Config.TRAINER)
    public int     trainerBox              = 256;
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
    public boolean trainersTradeItems      = true;
    @Configure(category = Config.TRAINER)
    public boolean trainersTradeMobs       = true;
    @Configure(category = Config.TRAINER)
    public boolean cullNoMobs              = false;
    @Configure(category = Config.TRAINER)
    public boolean trainerAIPause          = true;
    @Configure(category = Config.TRAINER)
    public int     aiPauseDistance         = 64;
    @Configure(category = Config.TRAINER)
    public int     trainer_crowding_radius = 16;
    @Configure(category = Config.TRAINER)
    public int     trainer_crowding_number = 5;

    @Configure(category = Config.TRAINER)
    public String trainer_defeat_reward = "{\"values\":{\"id\":\"minecraft:emerald\",\"n\":\"1\"}}";

    // Energy Sihpon related options
    @Configure(category = Config.MACHINE)
    public int maxOutput = 256;

    // Energy Sihpon related options
    @Configure(category = Config.MACHINE)
    public int siphonUpdateRate = 100;
    @Configure(category = Config.MACHINE)
    public int energyHungerCost = 5;

    @Configure(category = Config.MACHINE)
    public boolean wirelessSiphons = true;

    @Configure(category = Config.MACHINE)
    public String powerFunction = "a*x/10";

    // Cloning related options
    @Configure(type = Type.CLIENT, category = Config.MACHINE)
    public boolean expandedDNATooltips      = true;
    @Configure(category = Config.MACHINE)
    public int     fossilReanimateCost      = 50000;
    @Configure(category = Config.MACHINE)
    public boolean anyReanimate             = true;
    @Configure(category = Config.MACHINE)
    public boolean autoAddFossilDNA         = true;
    @Configure(category = Config.MACHINE)
    public String  clonerEfficiencyFunction = "x";

    // Options related to warp pad
    @Configure(category = Config.MACHINE)
    public String  warpPadCostFunction = "(dx)*(dx) + (dy)*(dy) + (dz)*(dz) + (5*dw)^4";
    @Configure(category = Config.MACHINE)
    public boolean warpPadEnergy       = true;
    @Configure(category = Config.MACHINE)
    public int     warpPadMaxEnergy    = 10000000;

    // Options related to daycare block.
    @Configure(category = Config.MACHINE)
    public int     dayCarePowerPerFuel = 256;
    @Configure(category = Config.MACHINE)
    public int     dayCareTickRate     = 20;
    @Configure(category = Config.MACHINE)
    public String  dayCarePowerPerExp  = "0.5";
    @Configure(category = Config.MACHINE)
    public String  dayCareExpFunction  = "n/10";
    @Configure(category = Config.MACHINE)
    public boolean dayCareBreedSpeedup = true;
    @Configure(category = Config.MACHINE)
    public int     dayCareBreedAmount  = 10;

    @Configure(category = Config.MACHINE)
    public int dayCareLvl100EffectiveLevel = 30;

    // AFA related options
    @Configure(category = Config.MACHINE)
    public int    afaShinyRate         = 4096;
    @Configure(category = Config.MACHINE)
    public String afaCostFunction      = "(d^3)/(10 + 5*l)";
    @Configure(category = Config.MACHINE)
    public String afaCostFunctionShiny = "(d^3)/10";
    @Configure(category = Config.MACHINE)
    public int    afaMaxEnergy         = 3200;
    @Configure(category = Config.MACHINE)
    public int    afaTickRate          = 5;

    @Configure(category = Config.BAG, type = Type.SERVER)
    public boolean bagsHoldEverything  = false;
    @Configure(category = Config.BAG, type = Type.SERVER)
    public boolean bagsHoldFilledCubes = false;
    @Configure(category = Config.BAG, type = Type.SERVER)
    public int     bagPages            = 32;

    public boolean loaded = false;

    public Config()
    {
        super(PokecubeAdv.MODID);
    }

    @Override
    public void onUpdated()
    {
        if (!this.loaded) return;

        EnergyHandler.initParser();
        BaseGeneticsTile.initParser(this.clonerEfficiencyFunction);
        WarppadTile.initParser(this.warpPadCostFunction);
        DaycareTile.initParser(this.dayCarePowerPerExp, this.dayCareExpFunction);
        AfaTile.initParser(this.afaCostFunction, this.afaCostFunctionShiny);
        this.dayCareTickRate = Math.max(1, this.dayCareTickRate);
        this.afaTickRate = Math.max(1, this.afaTickRate);
        this.trainerAgroRate = Math.max(1, this.trainerAgroRate);

        if (this.autoAddFossilDNA) for (final Entry<String, ItemFossil> fossil : ItemGenerator.fossils.entrySet())
        {
            final String name = fossil.getKey();
            final ItemStack stack = new ItemStack(fossil.getValue());
            final SpeciesGene gene = new SpeciesGene();
            final SpeciesInfo info = gene.getValue();
            info.entry = Database.getEntry(name);
            final Alleles genes = new Alleles(gene, gene);
            ClonerHelper.registerDNA(new DNAPack(name, genes, 1), stack);
        }
    }

}
