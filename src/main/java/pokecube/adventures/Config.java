package pokecube.adventures;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig.Type;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.daycare.DaycareTile;
import pokecube.adventures.blocks.genetics.helper.BaseGeneticsTile;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.utils.EnergyHandler;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final Config instance = new Config();

    @Configure(comment = "Configs related to the various machine blocks added.")
    private static final String MACHINE = "machine";
    @Configure(comment = "Configs related to trainer AI, trainer spawning, etc.")
    private static final String TRAINER = "trainers";
    private static final String BAG     = "bag";

    @Configure(category = Config.TRAINER, comment = "Anything that is an INPC will be made into a trainer. [Default: true]")
    public boolean      npcsAreTrainers = true;
    @Configure(category = Config.TRAINER, comment = "Trainers can go in snag cubes. [Default: false]")
    public boolean      canSnagTrainers = false;
    @Configure(category = Config.TRAINER, comment = "MobIDs listed here will be added as custom trainers if npcsAreTrainers is true.\n"
            + "this is for mobs that are not INPCs, but should be.")
    public List<String> custom_trainers = Lists.newArrayList("player_mobs:player_mob");

    @Configure(category = Config.TRAINER, comment = "This is the time in ticks, in which a trainer will go on cooldown"
            + "\nafter a player wins or loses a battle. [Default: 5000]")
    public int trainerCooldown     = 5000;
    @Configure(category = Config.TRAINER, comment = "How far trainers will check for targets to battle.\n"
            + "This is then modified by trainer_min_rep and trainer_max_rep for the actual sight distance.\n"
            + "The formula used is:\n" + "range_modified = range * (max_rep - rep) / (max_rep - min_rep)\n"
            + "range_modified is capped at 2 * range, and lower limit of 0. [Default: 8]")
    public int trainerSightRange   = 8;
    @Configure(category = Config.TRAINER, comment = "Time in ticks the trainer will be on cooldown after a battle\n"
            + "for targets other than the one they were battling. [Default: 50]")
    public int trainerBattleDelay  = 50;
    @Configure(category = Config.TRAINER, comment = "This is the delay in ticks between the trainer deciding to send out a pokemob, and actually doing so. [Default: 50]")
    public int trainerSendOutDelay = 50;
    @Configure(category = Config.TRAINER, comment = "Trainers will look for targets every this many ticks. [Default: 20]")
    public int trainerAgroRate     = 20;

    @Configure(category = Config.TRAINER, comment = "Trainers' pokemobs will gain exp as they battle. [Default: true]")
    public boolean trainerslevel           = true;
    @Configure(category = Config.TRAINER, comment = "Trainers will spawn naturally. [Default: true]")
    public boolean trainerSpawn            = true;
    @Configure(category = Config.TRAINER, comment = "This determines how sparsely trainers spawn.\n"
            + "there will only be trainerDensity trainers spawn every this far\n"
            + "excluding special spawns like villages. [Default: 256]")
    public int     trainerBox              = 256;
    @Configure(category = Config.TRAINER, comment = "How many trainers spawn in trainerBox. [Default: 2]")
    public double  trainerDensity          = 2;
    @Configure(category = Config.TRAINER, comment = "Pokemobs can hurt NPCs. [Default: false]")
    public boolean pokemobsHarmNPCs        = false;
    @Configure(category = Config.TRAINER, comment = "Trainers will occasionally battle each other. [Default: true]")
    public boolean trainersBattleEachOther = true;
    @Configure(category = Config.TRAINER, comment = "Trainers will occasionally battle wild pokemobs. [Default: true]")
    public boolean trainersBattlePokemobs  = true;
    @Configure(category = Config.TRAINER, comment = "If the trainer does not see its target for this many ticks, it will give up the battle. [Default: 100]")
    public int     trainerDeAgressTicks    = 100;
    @Configure(category = Config.TRAINER, comment = "Trainers will occasionally mate to produce more trainers. [Default: true]")
    public boolean trainersMate            = true;
    @Configure(category = Config.TRAINER, comment = "Trainers that are non-aggressive (ie. on cooldown or bribed) will offer item trades. [Default: true]")
    public boolean trainersTradeItems      = true;
    @Configure(category = Config.TRAINER, comment = "Trainers which are non-aggressive, might offer their pokemobs as trades for your pokemobs. [Default: true]")
    public boolean trainersTradeMobs       = true;
    @Configure(category = Config.TRAINER, comment = "Trainers with no pokemobs will despawn. [Default: false]")
    public boolean cullNoMobs              = false;
    @Configure(category = Config.TRAINER, comment = "If there are no players within aiPauseDistance, the trainer will not tick. [Default: true]")
    public boolean trainerAIPause          = true;
    @Configure(category = Config.TRAINER, comment = "See trainerAIPause. [Default: 64]")
    public int     aiPauseDistance         = 64;
    @Configure(category = Config.TRAINER, comment = "This is how far trainers will check to see if there are too many nearby to battle. [Default: 16]")
    public int     trainer_crowding_radius = 16;
    @Configure(category = Config.TRAINER, comment = "If there are more than this many trainers within trainer_crowding_radius,\n"
            + "then the trainers will not aggro to players. [Default: 5]")
    public int     trainer_crowding_number = 5;
    @Configure(category = Config.TRAINER, comment = "Minimum reputation for tracking, players at this or less are visible from twice as far. [Default: -100]")
    public int     trainer_min_rep         = -100;
    @Configure(category = Config.TRAINER, comment = "Maximum reputation for tracking, players at this or more, are not visible for auto-aggro. [Default: 100]")
    public int     trainer_max_rep         = 100;
    @Configure(category = Config.TRAINER, comment = "Reputation gain on killing a wild pokemob. [Default: 5]")
    public int     trainer_wild_kill_rep   = 5;
    @Configure(category = Config.TRAINER, comment = "Reputation gain on killing a tamed pokemob. [Default: 2]")
    public int     trainer_tame_kill_rep   = 2;

    @Configure(category = Config.TRAINER, comment = "if true, trainers will not aggro a player within pokecenter_radius of a Pokecenter. [Default: true]")
    public boolean no_battle_near_pokecenter = true;
    @Configure(category = Config.TRAINER, comment = "See no_battle_near_pokecenter. [Default: 10]")
    public int     pokecenter_radius         = 10;

    @Configure(category = Config.TRAINER, comment = "Default reward for trainers. [Default: \"minecraft:emerald\",\"n\":\"1\"]")
    public String trainer_defeat_reward = "{\"values\":{\"id\":\"minecraft:emerald\",\"n\":\"1\"}}";

    // Energy Sihpon related options
    @Configure(category = Config.MACHINE, comment = "The maximum forge energy per tick from an energy siphon. [Default: 256]")
    public int maxOutput = 256;

    // Energy Sihpon related options
    @Configure(category = Config.MACHINE, comment = "How often in ticks the siphon will search for new pokemobs to pull energy. [Default: 100]")
    public int siphonUpdateRate = 100;
    @Configure(category = Config.MACHINE, comment = "This scales the amount of hunger produced by pulling energy out of a pokemob. [Default: 5]")
    public int energyHungerCost = 5;

    @Configure(category = Config.MACHINE, comment = "Energy siphons can be linked to receiving blocks with the device linker. [Default: true]")
    public boolean wirelessSiphons = true;

    @Configure(category = Config.MACHINE, comment = "How much energy you get of a pokemob. [Default: a*x/10]\n"
            + " a is the max of spatk and atk.\n" + " x is the level of the pokemob.")
    public String powerFunction = "a*x/10";

    // Cloning related options
    @Configure(type = Type.CLIENT, category = Config.MACHINE, comment = "Displays additional tooltips in the genetics blocks. [Default: true]")
    public boolean expandedDNATooltips      = true;
    @Configure(category = Config.MACHINE, comment = "Amount of forge energy needed to revive a fossil. [Default: 10000]")
    public int     fossilReanimateCost      = 10000;
    @Configure(category = Config.MACHINE, comment = "Automatically registers DNA extraction recipes for fossils. [Default: true]")
    public boolean autoAddFossilDNA         = true;
    @Configure(category = Config.MACHINE, comment = "Used to scale the energy cost of genetics machines, x is the original energy input. [Default: x]")
    public String  clonerEfficiencyFunction = "x";

    // Options related to warp pad
    @Configure(category = Config.MACHINE, comment = "Energy cost of Warp Pad teleportation, dw is 0 for the same dimension, varies otherwise. [Default: (dx)*(dx) + (dy)*(dy) + (dz)*(dz) + (5*dw)^4]")
    public String  warpPadCostFunction = "(dx)*(dx) + (dy)*(dy) + (dz)*(dz) + (5*dw)^4";
    @Configure(category = Config.MACHINE, comment = "Warp pads require energy. [Default: true]")
    public boolean warpPadEnergy       = true;
    @Configure(category = Config.MACHINE, comment = "Warp pads can charge up to this much stored energy. [Default: 10000000]")
    public int     warpPadMaxEnergy    = 10000000;

    // Options related to daycare block.
    @Configure(category = Config.MACHINE, comment = "How much daycare power is produced per item of fuel consumed. [Default: 256]")
    public int     dayCarePowerPerFuel = 256;
    @Configure(category = Config.MACHINE, comment = "Daycares will only run every this many ticks. [Default: 20]")
    public int     dayCareTickRate     = 20;
    @Configure(category = Config.MACHINE, comment = "Amount of daycare power required to give exp to a pokemob, variables are:\n"
            + " x - pokemob's current exp\n" + " l - pokemob's current lvl\n"
            + " n - pokemob's needed exp from current level to next. [Default: 0.5]")
    public String  dayCarePowerPerExp  = "0.5";
    @Configure(category = Config.MACHINE, comment = "Amount of exp given per daycare tick, variables are:\n"
            + " x - pokemob's current exp\n" + " l - pokemob's current lvl\n"
            + " n - pokemob's needed exp from current level to next. [Default: n/10]")
    public String  dayCareExpFunction  = "n/10";
    @Configure(category = Config.MACHINE, comment = "Daycares will also speed up the breeding time. [Default: true]")
    public boolean dayCareBreedSpeedup = true;
    @Configure(category = Config.MACHINE, comment = "How many effective ticks are added for breeding time per daycare tick. [Default: 10]")
    public int     dayCareBreedAmount  = 10;

    @Configure(category = Config.MACHINE, comment = "Effective cost of a lvl 100 pokemob\n"
            + "this is for applying breeding tick stuff\n" + "even though lvl 100 can't gain exp. [Default: 30]")
    public int dayCareLvl100EffectiveLevel = 30;

    // AFA related options
    @Configure(category = Config.MACHINE, comment = "How likely an AFA is to result in shiny mobs nearby if it has a shiny charm in it. [Default: 4096]")
    public int    afaShinyRate         = 4096;
    @Configure(category = Config.MACHINE, comment = "Energy cost of the AFA for running in ability mode\n"
            + " l is the level of the mob\n" + " d is the range of the AFA. [Default: (d^3)/(10 + 5*l)]")
    public String afaCostFunction      = "(d^3)/(10 + 5*l)";
    @Configure(category = Config.MACHINE, comment = "Energy cost of the AFA when running in shiny mode\n "
            + "d is the range of the AFA. [Default: (d^3)/10]")
    public String afaCostFunctionShiny = "(d^3)/10";
    @Configure(category = Config.MACHINE, comment = "Maximum energy in the AFA, this effectively sets a max range for it to affect, based on the costs. [Default: 3200]")
    public int    afaMaxEnergy         = 3200;
    @Configure(category = Config.MACHINE, comment = "Afa ticks every this many ticks. [Default: 5]")
    public int    afaTickRate          = 5;

    @Configure(category = Config.BAG, type = Type.SERVER, comment = "The bag can hold any item. [Default: false]")
    public boolean bagsHoldEverything  = false;
    @Configure(category = Config.BAG, type = Type.SERVER, comment = "The bag can hold filled pokecubes. [Default: false]")
    public boolean bagsHoldFilledCubes = false;
    @Configure(category = Config.BAG, type = Type.SERVER, comment = "The number of pages the bag has. [Default: 32]")
    public int     bagPages            = 32;

    public boolean loaded = false;

    private final List<ResourceLocation> customTrainerTypes = Lists.newArrayList();

    public Config()
    {
        super(PokecubeAdv.MODID);
    }

    @Override
    public void onUpdated()
    {
        if (!this.loaded) return;
        this.customTrainerTypes.clear();
        this.custom_trainers.forEach(s -> this.customTrainerTypes.add(new ResourceLocation(s)));

        EnergyHandler.initParser();
        BaseGeneticsTile.initParser(this.clonerEfficiencyFunction);
        WarppadTile.initParser(this.warpPadCostFunction);
        DaycareTile.initParser(this.dayCarePowerPerExp, this.dayCareExpFunction);
        AfaTile.initParser(this.afaCostFunction, this.afaCostFunctionShiny);
        this.dayCareTickRate = Math.max(1, this.dayCareTickRate);
        this.afaTickRate = Math.max(1, this.afaTickRate);
        this.trainerAgroRate = Math.max(1, this.trainerAgroRate);
        RecipeClone.ENERGYCOST = this.fossilReanimateCost;
    }

    public boolean shouldBeCustomTrainer(final LivingEntity mob)
    {
        if (!this.npcsAreTrainers) return false;
        if (mob instanceof INPC) return true;
        return this.customTrainerTypes.contains(mob.getType().getRegistryName());
    }

}
