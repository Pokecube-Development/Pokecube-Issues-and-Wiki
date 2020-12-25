package pokecube.core.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.ai.tasks.idle.IdleWalkTask;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.pokemob.SpawnEvent.FunctionVariance;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.AITools;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class Config extends ConfigData
{
    public static final String spawning   = "spawning";
    public static final String database   = "database";
    public static final String world      = "generation";
    public static final String mobAI      = "ai";
    public static final String moves      = "moves";
    public static final String misc       = "misc";
    public static final String perms      = "permissions";
    public static final String client     = "client";
    public static final String advanced   = "advanced";
    public static final String healthbars = "healthbars";
    public static final String genetics   = "genetics";
    public static final String items      = "items";
    public static final String dynamax    = "dynamax";

    public static Config instance;

    private static Config defaults = new Config();

    private static SoundEvent getRegisteredSoundEvent(final String id)
    {

        final SoundEvent soundevent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(id));
        if (soundevent == null) throw new IllegalStateException("Invalid Sound requested: " + id);
        else return soundevent;
    }

    @Configure(category = Config.misc, comment = "Scaling factor for EXP yield from fighting another player's pokemobs")
    public double pvpExpMultiplier = 0.5;
    @Configure(category = Config.misc, comment = "Scales EXP yield from fighting non-player owned pokemobs")
    public double expScaleFactor   = 1;

    @Configure(category = Config.misc, comment = "Mob IDs which are blacklisted from going into snag cubes")
    public List<String> snag_cube_blacklist = Lists.newArrayList("ender_dragon", "wither");

    @Configure(category = Config.misc, comment = "If true, will automatically add interactions such as water from water types, and lighting torches on fire types.")
    public boolean defaultInteractions  = true;
    @Configure(category = Config.misc, comment = "If true, the mob's favourite berry can be used to speed up breeding.")
    public boolean berryBreeding        = true;
    @Configure(category = Config.misc, comment = "If true, legendary pokemobs can breed.")
    public boolean legendsBreed         = false;
    @Configure(category = Config.misc, comment = "If true, using a bed will heal your pokemobs")
    public boolean bedsHeal             = true;
    /** does defeating a tame pokemob give exp */
    @Configure(category = Config.misc, comment = "If true, defeating an NPC's pokemobs gives exp")
    public boolean trainerExp           = true;
    @Configure(category = Config.misc, comment = "If true, your items which fit in PC will be sent there when you die")
    public boolean pcOnDrop             = true;
    @Configure(category = Config.misc, comment = "If false, the PC can hold any item")
    public boolean pcHoldsOnlyPokecubes = true;
    @Configure(category = Config.misc, comment = "If true, you will be prompted to choose a pokemob when creating a world, without having to look for a professor")
    /** is there a choose first gui on login */
    public boolean guiOnLogin           = false;
    @Configure(category = Config.misc, comment = "If true, defeating a player's pokemobs gives exp")
    /** does defeating a tame pokemob give exp */
    public boolean pvpExp               = false;

    @Configure(category = Config.misc, comment = "A list of custom sounds to register")
    public List<String> customSounds = Lists.newArrayList();

    @Configure(category = Config.misc, comment = "A list of NBT tags to remove before saving the pokemob in its pokecube")
    public List<String> persistent_tag_blacklist = Lists.newArrayList();

    @Configure(category = Config.perms, comment = "")
    public boolean permsCapture         = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsCaptureSpecific = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsHatch           = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsHatchSpecific   = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsSendOut         = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsSendOutSpecific = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsRide            = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsRideSpecific    = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsFly             = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsFlySpecific     = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsSurf            = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsSurfSpecific    = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsDive            = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsDiveSpecific    = false;
    @Configure(category = Config.perms, comment = "")
    public boolean permsMoveAction      = false;

    // Move Use related settings
    @Configure(category = Config.moves, comment = "The \"range\" of contact moves, if not 0, they will apply damage like ranged attacks")
    public double  contactAttackDistance        = 0;
    @Configure(category = Config.moves, comment = "How far ranged attacks will be able to hit from")
    public double  rangedAttackDistance         = 16;
    @Configure(category = Config.moves, comment = "Can be used to scale damage from contact moves")
    public double  contactAttackDamageScale     = 1;
    @Configure(category = Config.moves, comment = "Can be used to scale damage from ranged moves")
    public double  rangedAttackDamageScale      = 1;
    @Configure(category = Config.moves, comment = "Scaling factor on explosion type moves's block damage")
    public double  blastStrength                = 100;
    @Configure(category = Config.moves, comment = "Maximum radius of effect of an explosion type move")
    public int     blastRadius                  = 16;
    @Configure(category = Config.moves, comment = "A cap on how much damage a wild pokemob can deal to a player")
    public int     maxWildPlayerDamage          = 10;
    @Configure(category = Config.moves, comment = "A cap on how much damage a tame pokemob can deal to a player")
    public int     maxOwnedPlayerDamage         = 10;
    @Configure(category = Config.moves, comment = "A Scaling factor on damage when a wild pokemob attempts to hit a player")
    public double  wildPlayerDamageRatio        = 1;
    @Configure(category = Config.moves, comment = "This fraction of the damage dealt is also applied as magic damage to the player, this is for wild pokemobs")
    public double  wildPlayerDamageMagic        = 0.1;
    @Configure(category = Config.moves, comment = "A Scaling factor on damage when a tame pokemob attempts to hit a player")
    public double  ownedPlayerDamageRatio       = 1;
    @Configure(category = Config.moves, comment = "This fraction of the damage dealt is also applied as magic damage to the player, this is for tame pokemobs")
    public double  ownedPlayerDamageMagic       = 0.1;
    @Configure(category = Config.moves, comment = "This is a scaling factor for damage dealt py pokemobs to mobs which are neither pokemobs, players or NPCs")
    public double  pokemobToOtherMobDamageRatio = 1;
    @Configure(category = Config.moves, comment = "This is a scaling factor for damage dealy by pokemobs to NPCs")
    public double  pokemobToNPCDamageRatio      = 1;
    @Configure(category = Config.moves, comment = "This is the hunger cost per item smelted by fire type moves")
    public int     baseSmeltingHunger           = 100;
    @Configure(category = Config.moves, comment = "If true, only pokemobs can deal damage to pokemobs")
    public boolean onlyPokemobsDamagePokemobs   = false;
    @Configure(category = Config.moves, comment = "This scales how much damage players can deal to pokemobs")
    public double  playerToPokemobDamageScale   = 1;
    @Configure(category = Config.moves, comment = "If true, enables world effects of fire type moves, such as: lighting fires, smelting, etc")
    public boolean defaultFireActions           = true;
    @Configure(category = Config.moves, comment = "If true, enables world effects of water type moves")
    public boolean defaultWaterActions          = true;
    @Configure(category = Config.moves, comment = "If true, enables world effects of electric type moves")
    public boolean defaultElectricActions       = true;
    @Configure(category = Config.moves, comment = "If true, enables world effects of ice type moves")
    public boolean defaultIceActions            = true;

    // AI Related settings
    @Configure(category = Config.mobAI, comment = "Determines how quickly mobs want to mate again, larger values are faster")
    public int    mateMultiplier     = 1;
    @Configure(category = Config.mobAI, comment = "Approximate number of ticks between breeding, this number cannot be less than 600")
    public int    breedingDelay      = 4000;
    @Configure(category = Config.mobAI, comment = "Maximum time for eggs to hatch, the average number is about half this")
    public int    eggHatchTime       = 10000;
    @Configure(category = Config.mobAI, comment = "If cull is enabled, idle wild mobs will vanish if exceeding this distance from a player, requires aiDisableDistance to be larger")
    public int    cullDistance       = 96;
    @Configure(category = Config.mobAI, comment = "If despawn is enabled, when mobs exceed cullDistance, and are within aiDisableDistance, they will vanish after this many ticks if no player get back in range")
    public int    despawnTimer       = 2000;
    @Configure(category = Config.mobAI, comment = "Wild pokemobs may agress the player if they get closer than this distance")
    public int    mobAggroRadius     = 3;
    @Configure(category = Config.mobAI, comment = "Wild pokemobs will check for a player to attack every approximately this many ticks, large values decrease the chance of pokemob agression")
    public int    mobAgroRate        = 200;
    @Configure(category = Config.mobAI, comment = "This is approximately how long a pokemob will live before it takes hunger damage, larger values will require less food to keep alive")
    public int    pokemobLifeSpan    = 8000;
    @Configure(category = Config.mobAI, comment = "This is a cooldown between the pokemob deciding to attack a player, and the pokemob actually doing so, this gives some warning you are about to be attacked")
    public int    pokemobagressticks = 100;
    @Configure(category = Config.mobAI, comment = "This is how often wild pokemobs make random sounds, higher numbers result in less noise")
    public int    idleSoundRate      = 100;
    @Configure(category = Config.mobAI, comment = "This scales how loud wild pokemob sounds are, lower numbers are lower volumes")
    public double idleSoundVolume    = 0.25;

    @Configure(category = Config.mobAI, comment = "This number multiplied by mobSpawnNumber is how many mobs can be in an area before pokemobs stop breeding, this is for wild pokemobs")
    public double mateDensityWild       = 2;
    @Configure(category = Config.mobAI, comment = "This scales the amount of vanilla exp that pokemobs drop on death")
    public double expFromDeathDropScale = 1;
    @Configure(category = Config.mobAI, comment = "This number multiplied by mobSpawnNumber is how many mobs can be in an area before pokemobs stop breeding, this is for tame pokemobs")
    public double mateDensityPlayer     = 4;

    @Configure(category = Config.mobAI, comment = "if true, pokemobs will vanish if they are more than cullDistance from a player")
    public boolean cull                  = false;
    @Configure(category = Config.mobAI, comment = "if true, pokemobs may despawn if more than cullDistance from a player")
    public boolean despawn               = true;
    @Configure(category = Config.mobAI, comment = "if true, hungry lithovores might eat gravel to nothing")
    public boolean pokemobsEatGravel     = false;
    @Configure(category = Config.mobAI, comment = "if true, hungy lithovores will eat rocks to cobble/gravel")
    public boolean pokemobsEatRocks      = true;
    @Configure(category = Config.mobAI, comment = "if ture, hungry herbivores will eat random plants")
    public boolean pokemobsEatPlants     = true;
    @Configure(category = Config.mobAI, comment = "if true, there will be a warning in chat if a wild pokemob agresses you")
    public boolean pokemobagresswarning  = true;
    @Configure(category = Config.mobAI, comment = "if true, player's pokemobs can hit them with AOE moves and misses")
    public boolean pokemobsDamageOwner   = false;
    @Configure(category = Config.mobAI, comment = "if ture, pokemobs can hurt players")
    public boolean pokemobsDamagePlayers = true;
    @Configure(category = Config.mobAI, comment = "if true, pokemob attacks can remove or destroy blocks in the world.")
    public boolean pokemobsDamageBlocks  = false;
    @Configure(category = Config.mobAI, comment = "if true, pokemobs drop their items from their loot tables")
    public boolean pokemobsDropItems     = true;
    @Configure(category = Config.mobAI, comment = "if true, explosion type moves make large physical explosions")
    public boolean explosions            = true;
    @Configure(category = Config.mobAI, comment = "if true, player's in the same team can still battle each other")
    public boolean teamsBattleEachOther  = true;

    @Configure(category = Config.mobAI, comment = "Pokemobs will forget their target if they get more than this far away")
    public int chaseDistance      = 32;
    @Configure(category = Config.mobAI, comment = "This is the target distance between two pokemobs during a fight")
    public int combatDistance     = 4;
    @Configure(category = Config.mobAI, comment = "If a pokemob is more than this far from a player, it will freeze and not tick. This distance must be larger than cullDistance for cull or despawn to do anything")
    public int aiDisableDistance  = 32;
    @Configure(category = Config.mobAI, comment = "This is how often tame pokemobs look for items to collect, smaller values are faster, 1 is the lowest this can be")
    public int tameGatherDelay    = 20;
    @Configure(category = Config.mobAI, comment = "This is how often wild pokemobs look for items to collect, smaller values are faster, 1 is the lowest this can be")
    public int wildGatherDelay    = 200;
    @Configure(category = Config.mobAI, comment = "This is how far away tame pokemobs will look for items to collect")
    public int tameGatherDistance = 16;
    @Configure(category = Config.mobAI, comment = "This is how far away wild pokemobs will look for items to collect")
    public int wildGatherDistance = 8;

    @Configure(category = Config.mobAI, comment = "If true, tame pokemobs will try to gather items")
    public boolean tameGather  = true;
    @Configure(category = Config.mobAI, comment = "If true, wild pokemobs will try to gather items")
    public boolean wildGather  = false;
    @Configure(category = Config.mobAI, comment = "If true, players can fly on valid pokemobs")
    public boolean flyEnabled  = true;
    @Configure(category = Config.mobAI, comment = "If true, players can surf on valid pokemobs")
    public boolean surfEnabled = true;
    @Configure(category = Config.mobAI, comment = "If true, players can dive with valid pokemobs")
    public boolean diveEnabled = true;

    @Configure(category = Config.mobAI, comment = "A random sound from here is played when a pokemob dodges in combat")
    public List<String> dodgeSounds        = Lists.newArrayList("entity.witch.throw");
    @Configure(category = Config.mobAI, comment = "A random sound from here is played when a pokemob leaps in combat")
    public List<String> leapSounds         = Lists.newArrayList("entity.witch.throw");
    @Configure(category = Config.mobAI, comment = "Mobs with these entity tags will not be a valid agression targets for pokemobs")
    public List<String> aggroBlacklistTags = Lists.newArrayList();
    @Configure(category = Config.mobAI, comment = "Mobs with these entity IDs will not be valid agression targets for pokemobs")
    public List<String> aggroBlacklistIds  = Lists.newArrayList("minecraft:villager", "minecraft:armor_stand",
            "pokecube_adventures:trainer", "pokecube_adventures:leader");

    @Configure(category = Config.mobAI, comment = "Scaling factor on hunger from pokemob interactions, such as lighting torches, milking, etc")
    public double  interactHungerScale = 1;
    @Configure(category = Config.mobAI, comment = "Scaling factor on time delay between pokemob interactions, such as lighting torches, milking, etc")
    public double  interactDelayScale  = 1;
    @Configure(category = Config.mobAI, comment = "If true, shift right clicking a valid pokemob with a stick will place it on your shoulder")
    public boolean pokemobsOnShoulder  = true;
    @Configure(category = Config.mobAI, comment = "Fish-like pokemobs will occasionally look for fishing bobbers in this range")
    public int     fishHookBaitRange   = 16;

    @Configure(category = Config.mobAI, comment = "If true, pokemobs can have guard mode enabled, which makes them randomly murder things nearby")
    public boolean guardModeEnabled    = true;
    @Configure(category = Config.mobAI, comment = "How far pokemobs look for targets while guarding an area")
    public int     guardSearchDistance = 16;
    @Configure(category = Config.mobAI, comment = "Delay between guard mode ticks, lower is faster")
    public int     guardTickRate       = 20;

    @Configure(category = Config.mobAI, comment = "How often pokemobs update their nearby blocks list, lower is faster, higher is less server load")
    public int nearBlockUpdateRate = 5;
    @Configure(category = Config.mobAI, comment = "How often pokemobs look for something to hunt when hungry, lower is faster, but more server load")
    public int huntUpdateRate      = 5;

    @Configure(category = Config.mobAI, comment = "Scaling factor on pokemob flight speed while pathing normally")
    public double  flyPathingSpeedFactor  = 1.25f;
    @Configure(category = Config.mobAI, comment = "Scaling factor on pokemob swim speed while pathing normally")
    public double  swimPathingSpeedFactor = 1.25f;
    @Configure(category = Config.mobAI, comment = "Pokemobs will refuse to enter the pokecube again for this many ticks after a failed capture")
    public int     captureDelayTicks      = 0;
    @Configure(category = Config.mobAI, comment = "If true, pokemobs will need to execute an attack after breaking out of a cube before they can go into another to try to capture")
    public boolean captureDelayTillAttack = true;
    @Configure(category = Config.mobAI, comment = "How often pokemobs attempt to perform an idle action, such as walking, etc. Larger numbers are better for server performance, but result in less wandering of wild pokemobs")
    public int     idleTickRate           = 200;
    @Configure(category = Config.mobAI, comment = "Maximum distance a wild pokemob will try to move while idle wandering")
    public int     idleMaxPathWild        = 16;
    @Configure(category = Config.mobAI, comment = "Maximum distance a tame pokemob will try to move while idle wandering")
    public int     idleMaxPathTame        = 4;
    @Configure(category = Config.mobAI, comment = "How often the hunger system ticks, larger numbers will result in faster hunger, setting to -1 will disable hunger entirely")
    public int     hungerTickRate         = 1;
    @Configure(category = Config.mobAI, comment = "How likely pokemobs are to call for help from nearby mobs when they are attacked")
    public double  hordeRateFactor        = 0.1;
    @Configure(category = Config.mobAI, comment = "Scales combat leaping speed")
    public double  leapSpeedFactor        = 1;
    @Configure(category = Config.mobAI, comment = "Scales combat doding speed")
    public double  dodgeSpeedFactor       = 1;
    @Configure(category = Config.mobAI, comment = "How long the exit cube animation effects last for")
    public int     exitCubeDuration       = 40;

    public SoundEvent[] dodges = {};
    public SoundEvent[] leaps  = {};

    // World Gen and World effect settings
    @Configure(category = Config.world, comment = "if true, random explosive meteors will fall, though only one every meteorDistance")
    public boolean meteors             = true;
    @Configure(category = Config.world, comment = "Minimum distance between explosive meteors")
    public int     meteorDistance      = 3000;
    @Configure(category = Config.world, comment = "Maximum radius of effect of damage from explosive meteors")
    public int     meteorRadius        = 64;
    @Configure(category = Config.world, comment = "Scaling factor on blast yield of explosive meteors")
    public double  meteorScale         = 1.0;
    @Configure(category = Config.world, comment = "If true, will attempt to ensure there is a pokecenter at spawn, this can still fail however depending on worldgen specifics")
    public boolean doSpawnBuilding     = true;
    @Configure(category = Config.world, comment = "If true, will also do a blanket \"plant material\" check for cuttable and edible plants, rather than relying entirely on the block tags")
    public boolean autoPopulateLists   = true;
    @Configure(category = Config.world, comment = "If true, will reset some subbiomes when spawn checks apply there")
    public boolean refreshSubbiomes    = false;
    @Configure(category = Config.world, comment = "If true, will allow the generic berry item to be added to pokemob drop pools if no other berries are added")
    public boolean autoAddNullBerries  = false;
    @Configure(category = Config.world, comment = "Can be used to adjust rate of berries on trees growing, lower values result in faster berry growth")
    public int     leafBerryTicks      = 75;
    @Configure(category = Config.world, comment = "If false, subbiomes will not auto-detect, meaning they need to be placed manually, useful for adventure maps, etc")
    public boolean autoDetectSubbiomes = true;
    @Configure(category = Config.world, comment = "If true, fossil ore will be added to certain biomes")
    public boolean generateFossils     = true;
    // @Configure(category = Config.world, comment = "")
    // public boolean villagePokecenters = true;

    @Configure(category = Config.world, comment = "Pokecube structures will not spawn in these dimensions, unless specifically stated in the structure's spawn rules")
    public List<String> softWorldgenDimBlacklist = Lists.newArrayList(
    //@formatter:off
            "pokecube:secret_base",
            "pokecube_legends:distorted_world",
            "pokecube_legends:ultraspace"
            );
    //@formatter:on

    // @Configure(category = Config.world, comment = "")
    // public String baseSizeFunction = "8 + c/10 + h/10 + k/20";
    // @Configure(category = Config.world, comment = "")
    // public int baseMaxSize = 1;
    @Configure(category = Config.world, comment = "Structures listed here will have the relevant subbiome applied for if minecraft thinks that the block is inside the structure.")
    public List<String> structure_subbiomes = Lists.newArrayList(
    //@formatter:off
            "{\"struct\":\"pokecube:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"Village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"minecraft:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"Monument\",\"subbiome\":\"monument\"}",
            "{\"struct\":\"minecraft:monument\",\"subbiome\":\"monument\"}"
            );
    //@formatter:on
    @Configure(category = Config.world, comment = "If true, any structure not in structure_subbiomes will apply as ruins, unless something else sets it first (like the structure's spawn settings)")
    public boolean structs_default_ruins = true;

    @Configure(category = Config.world, comment = "Extra json files to check in pokecube:structures/ in data for worldgen rules, do not include .json or path in the name in this list!")
    public List<String> extraWorldgenDatabases = Lists.newArrayList();
    @Configure(category = Config.world, comment = "This is what the value in the structure data block will be replaced with to generate the professor")
    public String       professor_override     = "pokecube:mob:professor{\"name\":\"pokecube.professor.named:Cedar\",\"guard\":{\"time\":\"day\",\"roam\":0}}";

    // Mob Spawning settings
    @Configure(category = Config.spawning, comment = "if true, vanilla monsters will not spawn via normal spawning, this does not prevent mob spawners or special spawns")
    public boolean deactivateMonsters     = false;
    @Configure(category = Config.spawning, comment = "if true, vanilla monsters are removed entirely, similar to in peaceful mode")
    public boolean disableVanillaMonsters = false;
    @Configure(category = Config.spawning, comment = "similar to disableVanillaAnimals, but for vanilla animals")
    public boolean disableVanillaAnimals  = false;
    @Configure(category = Config.spawning, comment = "similar to deactivateMonsters, but for vanilla animals")
    public boolean deactivateAnimals      = true;
    @Configure(category = Config.spawning, comment = "if false, pokemobs will not spawn naturally")
    public boolean pokemonSpawn           = true;

    @Configure(category = Config.spawning, comment = "Legendary pokemobs will not naturally spawn below this level")
    public int    minLegendLevel       = 1;
    @Configure(category = Config.spawning, comment = "This number, multiplied by mobDensityMultiplier will determine how many pokemobs will spawn within maxSpawnRadius of the player, this occurs at a rate based on spawnRate")
    public int    mobSpawnNumber       = 10;
    @Configure(category = Config.spawning, comment = "see mobSpawnNumber's description")
    public double mobDensityMultiplier = 1;

    @Configure(category = Config.spawning, comment = "Wild mobs will be capped at this lvl for spawning")
    public int levelCap = 50;

    @Configure(category = Config.spawning, comment = "if false, levelCap is ignored")
    public boolean shouldCap = true;

    @Configure(category = Config.spawning, comment = "These determine what lvl pokemobs spawn based on location. If central is true, then the origin for the function is 0,0, otherwise it is world spawn. if radial is true, then the function takes the variable r, which is horizontal distance from the origin. Otherwise it takes x and y, which are the horizontal coordinates with respect to the origin.")
    public List<String> dimensionSpawnLevels = Lists.newArrayList(new String[] {
            //@formatter:off
            "{\"dim\":\"the_nether\",\"func\":\"abs((25)*(sin(x*8*10^-3)^3 + sin(y*8*10^-3)^3))\",\"radial\":false,\"central\":false}",
            "{\"dim\":\"overworld\",\"func\":\"abs((25)*(sin(x*10^-3)^3 + sin(y*10^-3)^3))\",\"radial\":false,\"central\":false}",
            "{\"dim\":\"the_end\",\"func\":\"1+r/200\",\"radial\":true,\"central\":true}"
            });//@formatter:on

    @Configure(category = Config.spawning, comment = "This is an additional function applied to spawn lvls after dimensionSpawnLevels, here x is the level chosen by dimensionSpawnLevels, and the output is the actual spawn lvl")
    public String spawnLevelVariance = "x + ceil(5*rand())";

    @Configure(category = Config.spawning, comment = "Pokemobs will not spawn in the dimensions listed here")
    public List<String> spawnDimBlacklist = Lists.newArrayList();
    @Configure(category = Config.spawning, comment = "If spawnWhitelisted is enabled, pokemobs will only spawn in these dimensions")
    public List<String> spawnDimWhitelist = Lists.newArrayList();
    @Configure(category = Config.spawning, comment = "Enables using spawnDimWhitelist to determine if a dimension is valid for spawn")
    public boolean      spawnWhitelisted  = false;

    @Configure(category = Config.spawning, comment = "This is how often the code attempts to spawn pokemobs near a player")
    public int spawnRate   = 2;
    @Configure(category = Config.spawning, comment = "Default radius of effect for repels, also applies to max spots")
    public int repelRadius = 16;

    // Gui/client settings
    @Configure(category = Config.client, type = Type.CLIENT)
    public String guiRef     = "top_left";
    @Configure(category = Config.client, type = Type.CLIENT)
    public String messageRef = "right_middle";
    @Configure(category = Config.client, type = Type.CLIENT)
    public String targetRef  = "top_right";
    @Configure(category = Config.client, type = Type.CLIENT)
    public String teleRef    = "top_right";

    @Configure(category = Config.client, type = Type.CLIENT)
    public List<Integer> guiPos         = Lists.newArrayList(new Integer[] { 0, 0 });
    @Configure(category = Config.client, type = Type.CLIENT)
    public List<Integer> telePos        = Lists.newArrayList(new Integer[] { 89, 17 });
    @Configure(category = Config.client, type = Type.CLIENT)
    public List<Integer> targetPos      = Lists.newArrayList(new Integer[] { 147, -42 });
    @Configure(category = Config.client, type = Type.CLIENT)
    public List<Integer> messagePos     = Lists.newArrayList(new Integer[] { -150, -100 });
    @Configure(category = Config.client, type = Type.CLIENT)
    public List<Integer> messagePadding = Lists.newArrayList(new Integer[] { 0, 0 });

    @Configure(category = Config.client, type = Type.CLIENT)
    public double guiSize          = 1;
    @Configure(category = Config.client, type = Type.CLIENT)
    public double teleSize         = 1;
    @Configure(category = Config.client, type = Type.CLIENT)
    public double targetSize       = 1;
    @Configure(category = Config.client, type = Type.CLIENT)
    public double messageSize      = 1;
    @Configure(category = Config.client, type = Type.CLIENT)
    public double captureVolume    = 0.2;
    @Configure(category = Config.moves, type = Type.CLIENT)
    public double moveVolumeCry    = 0.0625f;
    @Configure(category = Config.moves, type = Type.CLIENT)
    public double moveVolumeEffect = 0.25;

    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean guiDown                = true;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean guiAutoScale           = false;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean autoSelectMoves        = false;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean autoRecallPokemobs     = false;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean riddenMobsTurnWithLook = true;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean extraberries           = false;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean battleLogInChat        = false;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean pokeCenterMusic        = true;
    @Configure(category = Config.client, type = Type.CLIENT)
    public boolean preloadModels          = false;

    @Configure(category = Config.client, type = Type.CLIENT)
    public int messageWidth       = 150;;
    @Configure(category = Config.client, type = Type.CLIENT)
    public int autoRecallDistance = 32;

    @Configure(category = Config.advanced)
    boolean        reputs = false;
    @Configure(category = Config.advanced)
    public boolean debug  = false;

    @Configure(category = Config.advanced)
    public boolean vanilla_pokemobs = false;

    @Configure(category = Config.advanced)
    // DOLATER find more internal variables to add to this.
    public List<String> extraVars = Lists.newArrayList(new String[] { "jc:" + EventsHandler.juiceChance, "rc:"
            + EventsHandler.candyChance, "eggDpl:" + ItemPokemobEgg.PLAYERDIST, "eggDpm:" + ItemPokemobEgg.MOBDIST });

    @Configure(category = Config.advanced, comment = "Moves in here will ignore pokemobsDamageBlocks, and apply their effects regardless")
    public List<String> damageBlocksWhitelist = Lists.newArrayList(new String[] { "flash", "teleport", "dig", "cut",
            "rocksmash", "secretpower", "naturepower" });
    @Configure(category = Config.advanced, comment = "Moves in here will ignore pokemobsDamageBlocks and never apply their effects")
    public List<String> damageBlocksBlacklist = Lists.newArrayList();
    @Configure(category = Config.advanced, comment = "This is how much exp is given for killing a non-pokemob, h is the max health of the mob, and a is the amount of armour it had")
    public String       nonPokemobExpFunction = "h*(a+1)^2";
    @Configure(category = Config.advanced, comment = "If true, killing non-pokemobs will provide exp to pokemobs")
    public boolean      nonPokemobExp         = false;
    @Configure(category = Config.advanced, comment = "Teleporting via the move teleport will not work in any dimensions listed here")
    public List<String> blackListedTeleDims   = Lists.newArrayList();

    @Configure(category = Config.advanced)
    public boolean pokemobsAreAllFrozen = false;

    @Configure(category = Config.advanced)
    public int pokecubeAutoSendOutDelay = 20;

    @Configure(category = Config.advanced, comment = "If true, lakes will be removed from pokecube structure spawns, disable this if that interferes with other mod's worldgen!")
    public boolean lakeFeatureMixin = true;

    @Configure(category = Config.genetics)
    public String       epigeneticEVFunction = GeneticsManager.epigeneticFunction;
    @Configure(category = Config.genetics)
    public List<String> mutationRates        = GeneticsManager.getMutationConfig();

    @Configure(category = Config.database, comment = "Extra databases for use with datapacks that just want to add things, these will be added after the normal databases have loaded in.")
    public List<String> configDatabases = Lists.newArrayList(new String[] {
            "database/pokemobs/pokemobs_pokedex.json;database/pokemobs/pokemobs_spawns.json;database/pokemobs/pokemobs_interacts.json",
            "database/moves.json", "database/spawns.json" });

    @Configure(category = Config.database)
    public List<String> recipeDatabases = Lists.newArrayList(new String[] { "recipes" });
    @Configure(category = Config.database)
    public List<String> rewardDatabases = Lists.newArrayList(new String[] { "rewards" });

    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean doHealthBars         = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     maxDistance          = 24;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean renderInF1           = false;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean brightbars           = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public double  heightAbove          = 0.6;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean drawBackground       = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     backgroundPadding    = 2;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     backgroundHeight     = 6;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     barHeight            = 4;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     plateSize            = 25;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean showHeldItem         = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean showArmor            = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     hpTextHeight         = 14;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean showOnlyFocused      = false;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public boolean enableDebugInfo      = true;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     ownedNameColour      = 0x55FF55;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     otherOwnedNameColour = 0xFF5555;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     caughtNamedColour    = 0x5555FF;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     scannedNameColour    = 0x88FFFF;
    @Configure(category = Config.healthbars, type = Type.CLIENT)
    public int     unknownNameColour    = 0x888888;

    @Configure(category = Config.dynamax)
    public int    dynamax_cooldown = 6000;
    @Configure(category = Config.dynamax)
    public int    dynamax_duration = 250;
    @Configure(category = Config.dynamax)
    public double dynamax_scale    = 5.0;
    @Configure(category = Config.dynamax)
    public int    z_move_cooldown  = 2000;

    // Config options which are needed to by synchronized on both sides

    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "If true, dead wild pokemobs will vanish like normal mobs do on death")
    public boolean wildDeadDespawn = true;
    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "If true, dead tame pokemobs will vanish like normal mobs do on death, they will return to their pokecubes when they vanish")
    public boolean tameDeadDespawn = true;

    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "time it takes for the dead mobs to vanish if allowed")
    public int deadDespawnTimer = 20;
    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "if they did not vanish by this time, they will revive instead, wild ones at full hp, tame ones at 1 hp")
    public int deadReviveTimer  = 600;

    // ridden Speed multipliers
    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "scaling factor on riding speed while flying")
    public double flySpeedFactor    = 1;
    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "scaling factor on riding speed while in water")
    public double surfSpeedFactor   = 1;
    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "scaling factor on riding speed while on ground")
    public double groundSpeedFactor = 1;

    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "Flying will not be allowed in these dimensions")
    public List<String> blackListedFlyDims = Lists.newArrayList(new String[] { "the_end", "the_nether" });

    @Configure(category = Config.mobAI, type = Type.SERVER, comment = "Approximate cooldown for attacks in ticks, larger values will slow down combat")
    public int attackCooldown = 20;

    @Configure(category = Config.spawning, type = Type.SERVER, comment = "Pokemobs will not spawn further than this distance from a player")
    public int maxSpawnRadius = 32; // Synced for pokewatch
    @Configure(category = Config.spawning, type = Type.SERVER, comment = "Pokemobs will not spawn closer than this distance from a player")
    public int minSpawnRadius = 16; // Synced for pokewatch

    @Configure(category = Config.misc, type = Type.SERVER, comment = "If true, npcs will display their name tags, this is server config for syncing to clients for balance purposes")
    public boolean npcNameTags = true; // Synced for balance reasons

    @Configure(category = Config.misc, type = Type.SERVER, comment = "Global size scaling factor for pokemobs")
    public double scalefactor = 1;

    @Configure(category = Config.misc, type = Type.SERVER, comment = "Number of pages in the PC")
    public int pcPageCount = 32;

    @Configure(category = Config.advanced, type = Type.SERVER, comment = "Time it takes for a pokemob to evolve, note that recalling during this time will cancel the evolution!")
    public int evolutionTicks = 50;
    @Configure(category = Config.advanced, type = Type.SERVER, comment = "Distance that secret base radar works within")
    public int baseRadarRange = 64;

    @Configure(category = Config.advanced, type = Type.SERVER, comment = "Number of ender pearls required to teleport to a pokecenter in your current dimension, using the attack teleport")
    public int telePearlsCostSameDim  = 0;
    @Configure(category = Config.advanced, type = Type.SERVER, comment = "Number of ender pearls required to teleport to a pokecenter in a different diemsnion, using the attack teleport")
    public int telePearlsCostOtherDim = 16;

    @Configure(category = Config.advanced, type = Type.SERVER, comment = "Pokemobs larger than this are treated a bit differently to try to reduce lag when they move around")
    public double largeMobForSplit = 2;

    @Configure(category = Config.advanced, type = Type.SERVER, comment = "A spacing around a player which prevnts them from riding something too fast off the edge of the loaded area. This effectively limits the speed at which they can generate terrain")
    public double movementPauseThreshold = 32;

    public Config()
    {
        super(PokecubeCore.MODID);
    }

    @Override
    public void onUpdated()
    {
        // Ensure these values are in bounds.
        if (this.attackCooldown <= 0) this.attackCooldown = 1;
        if (this.spawnRate <= 0) this.spawnRate = 1;
        if (this.idleTickRate == 0) this.idleTickRate = 1;
        if (this.hungerTickRate == 0) this.hungerTickRate = 1;
        if (this.nearBlockUpdateRate <= 0) this.nearBlockUpdateRate = 1;
        if (this.huntUpdateRate <= 0) this.huntUpdateRate = 1;

        IdleWalkTask.IDLETIMER = this.idleTickRate;
        HungerTask.TICKRATE = this.hungerTickRate;

        // TODO Init secret bases resizing
        // DimensionSecretBase.init(baseSizeFunction);
        PokecubeTerrainChecker.initStructMap();
        WorldgenHandler.SOFTBLACKLIST.clear();
        for (final String s : this.softWorldgenDimBlacklist)
            WorldgenHandler.SOFTBLACKLIST.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(s)));
        WorldgenHandler.SOFTBLACKLIST.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(
                "pokecube:secret_base")));

        SpawnHandler.MAX_DENSITY = this.mobDensityMultiplier;
        SpawnHandler.MAXNUM = this.mobSpawnNumber;
        if (this.breedingDelay < 600) this.breedingDelay = 1000;

        AITools.initIDs();

        SpawnHandler.doSpawns = this.pokemonSpawn;
        SpawnHandler.lvlCap = this.shouldCap;
        SpawnHandler.capLevel = this.levelCap;
        SpawnHandler.initSpawnFunctions();
        SpawnHandler.refreshSubbiomes = this.refreshSubbiomes;
        SpawnHandler.DEFAULT_VARIANCE = new FunctionVariance(this.spawnLevelVariance);

        PokecubeSerializer.MeteorDistance = this.meteorDistance * this.meteorDistance;
        PokecubeMod.debug = this.debug;

        for (String s : this.recipeDatabases)
        {
            if (!s.endsWith(".json")) s = s + ".json";
            XMLRecipeHandler.recipeFiles.add(PokecubeItems.toPokecubeResource(s));
        }
        for (String s : this.rewardDatabases)
        {
            if (!s.endsWith(".json")) s = s + ".json";
            XMLRewardsHandler.recipeFiles.add(PokecubeItems.toPokecubeResource(s));
        }
        if (this.extraVars.size() != Config.defaults.extraVars.size())
        {
            final List<String> old = Lists.newArrayList(this.extraVars);
            this.extraVars = Lists.newArrayList(Config.defaults.extraVars);
            for (int i = 0; i < this.extraVars.size(); i++)
            {
                final String[] args1 = this.extraVars.get(i).split(":");
                final String key1 = args1[0];
                for (final String s : old)
                {
                    final String[] args2 = s.split(":");
                    final String key2 = args2[0];
                    if (key1.equals(key2))
                    {
                        this.extraVars.set(i, s);
                        break;
                    }
                }
            }
        }
        // DOLATER more internal variables
        for (final String s : this.extraVars)
        {
            final String[] args = s.split(":");
            final String key = args[0];
            final String value = args[1];
            if (key.equals("jc"))
            {
                EventsHandler.juiceChance = Double.parseDouble(value);
                continue;
            }
            if (key.equals("rc"))
            {
                EventsHandler.candyChance = Double.parseDouble(value);
                continue;
            }
            if (key.equals("eggDpl"))
            {
                ItemPokemobEgg.PLAYERDIST = Double.parseDouble(value);
                continue;
            }
            if (key.equals("eggDpm"))
            {
                ItemPokemobEgg.MOBDIST = Double.parseDouble(value);
                continue;
            }
        }

        if (this.mutationRates.size() != Config.defaults.mutationRates.size())
        {
            final List<String> old = Lists.newArrayList(this.mutationRates);
            this.mutationRates = Lists.newArrayList(Config.defaults.mutationRates);
            for (int i = 0; i < this.mutationRates.size(); i++)
            {
                final String[] args1 = this.mutationRates.get(i).split(" ");
                final String key1 = args1[0];
                for (final String s : old)
                {
                    final String[] args2 = s.split(" ");
                    final String key2 = args2[0];
                    if (key1.equals(key2))
                    {
                        this.mutationRates.set(i, s);
                        break;
                    }
                }
            }
        }

        for (final String s : this.mutationRates)
        {
            final String[] args = s.split(" ");
            final String key = args[0];
            try
            {
                final Float value = Float.parseFloat(args[1]);
                final ResourceLocation loc = new ResourceLocation(key);
                GeneticsManager.mutationRates.put(loc, value);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with mutation rate for " + s, e);
            }
        }

        Pokecube.snagblacklist.clear();
        Pokecube.snagblacklist.add(new ResourceLocation("player"));
        for (final String s : this.snag_cube_blacklist)
            Pokecube.snagblacklist.add(new ResourceLocation(s));

        PokecubeItems.resetTimeTags = this.reputs;

        if (this.configDatabases.size() != EnumDatabase.values().length) this.configDatabases = Lists.newArrayList(
                new String[] { "", "", "" });

        SpawnHandler.dimensionBlacklist.clear();
        for (final String i : this.spawnDimBlacklist)
        {
            final ResourceLocation key = new ResourceLocation(i);
            SpawnHandler.dimensionBlacklist.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key));
        }
        SpawnHandler.dimensionWhitelist.clear();
        for (final String i : this.spawnDimWhitelist)
        {
            final ResourceLocation key = new ResourceLocation(i);
            SpawnHandler.dimensionWhitelist.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key));
        }
        LogicMountedControl.BLACKLISTED.clear();
        for (final String i : this.blackListedFlyDims)
        {
            final ResourceLocation key = new ResourceLocation(i);
            LogicMountedControl.BLACKLISTED.add(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key));
        }

        boolean failed = false;
        if (this.dodgeSounds.size() == 0) failed = true;
        else
        {
            this.dodges = new SoundEvent[this.dodgeSounds.size()];
            for (int i = 0; i < this.dodgeSounds.size(); i++)
            {
                final String s = this.dodgeSounds.get(i);
                try
                {
                    final SoundEvent e = Config.getRegisteredSoundEvent(s);
                    this.dodges[i] = e;
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("No Sound for " + s, e);
                    failed = true;
                    break;
                }
            }
        }

        if (failed) this.dodges = new SoundEvent[] { SoundEvents.ENTITY_GENERIC_SMALL_FALL };

        failed = false;
        if (this.leapSounds.size() == 0) failed = true;
        else
        {
            this.leaps = new SoundEvent[this.leapSounds.size()];
            for (int i = 0; i < this.leapSounds.size(); i++)
            {
                final String s = this.leapSounds.get(i);
                try
                {
                    final SoundEvent e = Config.getRegisteredSoundEvent(s);
                    this.leaps[i] = e;
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.info("No Sound for " + s);
                    failed = true;
                    break;
                }
            }
        }

        if (failed) this.leaps = new SoundEvent[] { SoundEvents.ENTITY_GENERIC_SMALL_FALL };

        PokecubeManager.init();
    }
}
