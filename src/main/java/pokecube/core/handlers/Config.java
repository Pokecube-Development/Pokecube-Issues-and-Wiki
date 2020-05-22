package pokecube.core.handlers;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.dimension.DimensionType;
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
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.pokemob.SpawnEvent.FunctionVariance;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
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

    // Misc Settings
    @Configure(category = Config.misc)
    public boolean      pcHoldsOnlyPokecubes = true;
    @Configure(category = Config.misc)
    /** is there a choose first gui on login */
    public boolean      guiOnLogin           = false;
    @Configure(category = Config.misc)
    /** does defeating a tame pokemob give exp */
    public boolean      pvpExp               = false;
    @Configure(category = Config.misc)
    /** does defeating a tame pokemob give exp */
    public double       pvpExpMultiplier     = 0.5;
    @Configure(category = Config.misc)
    /** does defeating a tame pokemob give exp */
    public boolean      trainerExp           = true;
    @Configure(category = Config.misc)
    public boolean      pcOnDrop             = true;
    @Configure(category = Config.misc)
    public double       expScaleFactor       = 1;
    @Configure(category = Config.misc)
    public List<String> snag_cube_blacklist  = Lists.newArrayList("ender_dragon", "wither");
    @Configure(category = Config.misc)
    public boolean      defaultInteractions  = true;
    @Configure(category = Config.misc)
    public boolean      berryBreeding        = true;
    @Configure(category = Config.misc)
    public boolean      legendsBreed         = false;
    @Configure(category = Config.misc)
    public List<String> customSounds         = Lists.newArrayList();

    @Configure(category = Config.misc)
    public List<String> persistent_tag_blacklist = Lists.newArrayList();

    @Configure(category = Config.perms)
    public boolean permsCapture         = false;
    @Configure(category = Config.perms)
    public boolean permsCaptureSpecific = false;
    @Configure(category = Config.perms)
    public boolean permsHatch           = false;
    @Configure(category = Config.perms)
    public boolean permsHatchSpecific   = false;
    @Configure(category = Config.perms)
    public boolean permsSendOut         = false;
    @Configure(category = Config.perms)
    public boolean permsSendOutSpecific = false;
    @Configure(category = Config.perms)
    public boolean permsRide            = false;
    @Configure(category = Config.perms)
    public boolean permsRideSpecific    = false;
    @Configure(category = Config.perms)
    public boolean permsFly             = false;
    @Configure(category = Config.perms)
    public boolean permsFlySpecific     = false;
    @Configure(category = Config.perms)
    public boolean permsSurf            = false;
    @Configure(category = Config.perms)
    public boolean permsSurfSpecific    = false;
    @Configure(category = Config.perms)
    public boolean permsDive            = false;
    @Configure(category = Config.perms)
    public boolean permsDiveSpecific    = false;
    @Configure(category = Config.perms)
    public boolean permsMoveAction      = false;

    // Move Use related settings
    @Configure(category = Config.moves)
    public double  contactAttackDistance        = 0;
    @Configure(category = Config.moves)
    public double  rangedAttackDistance         = 16;
    @Configure(category = Config.moves)
    public double  contactAttackDamageScale     = 1;
    @Configure(category = Config.moves)
    public double  rangedAttackDamageScale      = 1;
    @Configure(category = Config.moves)
    /** Scaling factor for pokemob explosions */
    public double  blastStrength                = 100;
    @Configure(category = Config.moves)
    /** Scaling factor for pokemob explosions */
    public int     blastRadius                  = 16;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public int     maxWildPlayerDamage          = 10;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public int     maxOwnedPlayerDamage         = 10;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public double  wildPlayerDamageRatio        = 1;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public double  wildPlayerDamageMagic        = 0.1;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public double  ownedPlayerDamageRatio       = 1;
    @Configure(category = Config.moves)
    /** Capped damage to players by pok�mobs */
    public double  ownedPlayerDamageMagic       = 0.1;
    @Configure(category = Config.moves)
    /** Scaling factor for damage against not pokemobs */
    public double  pokemobToOtherMobDamageRatio = 1;
    @Configure(category = Config.moves)
    /** Scaling factor for damage against NPCs */
    public double  pokemobToNPCDamageRatio      = 1;
    @Configure(category = Config.moves)
    public int     baseSmeltingHunger           = 100;
    @Configure(category = Config.moves)
    public boolean onlyPokemobsDamagePokemobs   = false;
    @Configure(category = Config.moves)
    public double  playerToPokemobDamageScale   = 1;
    @Configure(category = Config.moves)
    public boolean defaultFireActions           = true;
    @Configure(category = Config.moves)
    public boolean defaultWaterActions          = true;
    @Configure(category = Config.moves)
    public boolean defaultElectricActions       = true;
    @Configure(category = Config.moves)
    public boolean defaultIceActions            = true;

    // AI Related settings
    @Configure(category = Config.mobAI)
    public int mateMultiplier     = 1;
    @Configure(category = Config.mobAI)
    public int mateAIRate         = 40;
    @Configure(category = Config.mobAI)
    public int breedingDelay      = 4000;
    @Configure(category = Config.mobAI)
    public int eggHatchTime       = 10000;
    /** distance for culling */
    @Configure(category = Config.mobAI)
    public int cullDistance       = 96;
    /** distance for culling */
    @Configure(category = Config.mobAI)
    public int despawnTimer       = 2000;
    @Configure(category = Config.mobAI)
    /** Distance to player needed to agress the player */
    public int mobAggroRadius     = 3;
    @Configure(category = Config.mobAI)
    /**
     * Approximately how many ticks between wild pokemobs running agro
     * checks.
     */
    public int mobAgroRate        = 200;
    @Configure(category = Config.mobAI)
    /**
     * Approximate number of ticks before pok�mob starts taking hunger
     * damage
     */
    public int pokemobLifeSpan    = 8000;
    @Configure(category = Config.mobAI)
    /** Warning time before a wild pokemob attacks a player */
    public int pokemobagressticks = 100;

    @Configure(category = Config.mobAI)
    public double mateDensityWild       = 2;
    @Configure(category = Config.mobAI)
    public double expFromDeathDropScale = 1;
    @Configure(category = Config.mobAI)
    public double mateDensityPlayer     = 4;

    @Configure(category = Config.mobAI)
    /** do wild pokemobs which leave cullDistance despawn immediately */
    public boolean cull                  = false;
    @Configure(category = Config.mobAI)
    public boolean despawn               = true;
    @Configure(category = Config.mobAI)
    /** Will lithovores eat gravel */
    public boolean pokemobsEatGravel     = false;
    @Configure(category = Config.mobAI)
    /** Will lithovores eat rocks */
    public boolean pokemobsEatRocks      = true;
    @Configure(category = Config.mobAI)
    /** Will herbivores eat plants */
    public boolean pokemobsEatPlants     = true;
    @Configure(category = Config.mobAI)
    /** Is there a warning before a wild pok�mob attacks the player. */
    public boolean pokemobagresswarning  = true;
    @Configure(category = Config.mobAI)
    public boolean pokemobsDamageOwner   = false;
    @Configure(category = Config.mobAI)
    public boolean pokemobsDamagePlayers = true;
    @Configure(category = Config.mobAI)
    public boolean pokemobsDamageBlocks  = false;
    @Configure(category = Config.mobAI)
    public boolean pokemobsDropItems     = true;
    @Configure(category = Config.mobAI)
    /** Do explosions occur and cause damage */
    public boolean explosions            = true;

    @Configure(category = Config.mobAI)
    public int chaseDistance      = 32;
    @Configure(category = Config.mobAI)
    public int combatDistance     = 4;
    @Configure(category = Config.mobAI)
    public int aiDisableDistance  = 32;
    @Configure(category = Config.mobAI)
    public int tameGatherDelay    = 20;
    @Configure(category = Config.mobAI)
    public int wildGatherDelay    = 200;
    @Configure(category = Config.mobAI)
    public int tameGatherDistance = 16;
    @Configure(category = Config.mobAI)
    public int wildGatherDistance = 8;

    @Configure(category = Config.mobAI)
    public boolean tameGather  = true;
    @Configure(category = Config.mobAI)
    public boolean wildGather  = false;
    @Configure(category = Config.mobAI)
    public boolean flyEnabled  = true;
    @Configure(category = Config.mobAI)
    public boolean surfEnabled = true;
    @Configure(category = Config.mobAI)
    public boolean diveEnabled = true;

    @Configure(category = Config.mobAI)
    public List<String> dodgeSounds         = Lists.newArrayList("entity.witch.throw");
    @Configure(category = Config.mobAI)
    public List<String> leapSounds          = Lists.newArrayList("entity.witch.throw");
    @Configure(category = Config.mobAI)
    public List<String> guardBlacklistClass = Lists.newArrayList("net.minecraft.entity.IMerchant",
            "net.minecraft.entity.INpc", "pokecube.core.items.pokemobeggs.EntityPokemobEgg",
            "net.minecraft.entity.IProjectile");
    @Configure(category = Config.mobAI)
    public List<String> guardBlacklistId    = Lists.newArrayList();

    @Configure(category = Config.mobAI)
    public double  interactHungerScale = 1;
    @Configure(category = Config.mobAI)
    public double  interactDelayScale  = 1;
    @Configure(category = Config.mobAI)
    public boolean pokemobsOnShoulder  = true;

    @Configure(category = Config.mobAI)
    public int fishHookBaitRange = 16;

    @Configure(category = Config.mobAI)
    public boolean guardModeEnabled = true;

    @Configure(category = Config.mobAI)
    public int guardSearchDistance = 16;
    @Configure(category = Config.mobAI)
    public int guardTickRate       = 20;

    @Configure(category = Config.mobAI)
    public int nearBlockUpdateRate = 5;
    @Configure(category = Config.mobAI)
    public int huntUpdateRate      = 5;

    // Used by pathfinder's movehelper for scaling speed in air and water.
    @Configure(category = Config.mobAI)
    public double  flyPathingSpeedFactor  = 1.25f;
    @Configure(category = Config.mobAI)
    public double  swimPathingSpeedFactor = 1.25f;
    @Configure(category = Config.mobAI)
    public boolean pokemobCollisions      = true;
    @Configure(category = Config.mobAI)
    public int     captureDelayTicks      = 0;
    @Configure(category = Config.mobAI)
    public boolean captureDelayTillAttack = true;
    @Configure(category = Config.mobAI)
    public int     idleTickRate           = 200;
    @Configure(category = Config.mobAI)
    public int     idleMaxPathWild        = 16;
    @Configure(category = Config.mobAI)
    public int     idleMaxPathTame        = 16;
    @Configure(category = Config.mobAI)
    public int     hungerTickRate         = 20;
    @Configure(category = Config.mobAI)
    public double  hordeRateFactor        = 1;
    @Configure(category = Config.mobAI)
    public double  leapSpeedFactor        = 1;
    @Configure(category = Config.mobAI)
    public double  dodgeSpeedFactor       = 1;
    @Configure(category = Config.mobAI)
    public int     exitCubeDuration       = 40;

    public SoundEvent[] dodges = {};
    public SoundEvent[] leaps  = {};

    // World Gen and World effect settings
    @Configure(category = Config.world)
    /** do meteors fall. */
    public boolean meteors              = true;
    @Configure(category = Config.world)
    public int     meteorDistance       = 3000;
    @Configure(category = Config.world)
    public int     meteorRadius         = 64;
    @Configure(category = Config.world)
    public double  meteorScale          = 1.0;
    @Configure(category = Config.world)
    public boolean doSpawnBuilding      = true;
    @Configure(category = Config.world)
    public boolean basesLoaded          = true;
    @Configure(category = Config.world)
    public boolean autoPopulateLists    = true;
    @Configure(category = Config.world)
    public boolean refreshSubbiomes     = false;
    @Configure(category = Config.world)
    public boolean autoAddNullBerries   = false;
    @Configure(category = Config.world)
    public int     cropGrowthTicks      = 75;
    @Configure(category = Config.world)
    public int     leafBerryTicks       = 75;
    @Configure(category = Config.world)
    public boolean autoDetectSubbiomes  = true;
    @Configure(category = Config.world)
    public boolean generateFossils      = true;
    @Configure(category = Config.world)
    public boolean villagePokecenters   = true;
    @Configure(category = Config.world)
    public boolean chunkLoadPokecenters = false;

    @Configure(category = Config.world)
    public String       baseSizeFunction    = "8 + c/10 + h/10 + k/20";
    @Configure(category = Config.world)
    public int          baseMaxSize         = 1;
    @Configure(category = Config.world)
    public List<String> structure_subbiomes = Lists.newArrayList(
    //@formatter:off
            "{\"struct\":\"pokecube:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"Village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"minecraft:village\",\"subbiome\":\"village\"}",
            "{\"struct\":\"Monument\",\"subbiome\":\"monument\"}",
            "{\"struct\":\"minecraft:monument\",\"subbiome\":\"monument\"}"
            );
    //@formatter:on
    @Configure(category = Config.world)
    public boolean structs_default_ruins = true;

    @Configure(category = Config.world)
    public List<String> extraWorldgenDatabases = Lists.newArrayList();
    @Configure(category = Config.world)
    public int          spawnDimension         = 0;
    @Configure(category = Config.world)
    public String       professor_override     = "pokecube:mob:professor{\"name\":\"pokecube.professor.named:Cedar\",\"guard\":{\"time\":\"day\",\"roam\":0}}";

    // Mob Spawning settings
    @Configure(category = Config.spawning)
    /** Do monsters not spawn. */
    public boolean deactivateMonsters     = false;
    @Configure(category = Config.spawning)
    /** do monster spawns get swapped with shadow pokemobs */
    public boolean disableVanillaMonsters = false;
    @Configure(category = Config.spawning)
    public boolean disableVanillaAnimals  = false;
    @Configure(category = Config.spawning)
    /** do animals not spawn */
    public boolean deactivateAnimals      = true;
    @Configure(category = Config.spawning)
    /** do Pokemobs spawn */
    public boolean pokemonSpawn           = true;
    @Configure(category = Config.spawning)
    /** Minimum level legendaries can spawn at. */
    public int     minLegendLevel         = 1;
    @Configure(category = Config.spawning)
    public int     mobSpawnNumber         = 10;
    @Configure(category = Config.spawning)
    public double  mobDensityMultiplier   = 1;

    @Configure(category = Config.spawning)
    public int levelCap = 50;

    @Configure(category = Config.spawning)
    public boolean shouldCap = true;

    @Configure(category = Config.spawning)
    public List<String> dimensionSpawnLevels = Lists.newArrayList(new String[] {
            //@formatter:off
            "{\"dim\":\"the_nether\",\"func\":\"abs((25)*(sin(x*8*10^-3)^3 + sin(y*8*10^-3)^3))\",\"radial\":false,\"central\":false}",
            "{\"dim\":\"overworld\",\"func\":\"abs((25)*(sin(x*10^-3)^3 + sin(y*10^-3)^3))\",\"radial\":false,\"central\":false}",
            "{\"dim\":\"the_end\",\"func\":\"1+r/200\",\"radial\":true,\"central\":true}"
            });//@formatter:on

    @Configure(category = Config.spawning)
    public boolean expFunction        = false;
    @Configure(category = Config.spawning)
    public String  spawnLevelVariance = "x + ceil(5*rand())";

    @Configure(category = Config.spawning)
    public List<String> spawnDimBlacklist = Lists.newArrayList();
    @Configure(category = Config.spawning)
    public List<String> spawnDimWhitelist = Lists.newArrayList();
    @Configure(category = Config.spawning)
    public boolean      spawnWhitelisted  = false;
    @Configure(category = Config.spawning)
    /** Spawns run once every this many ticks.. */
    public int          spawnRate         = 20;
    @Configure(category = Config.spawning)
    /** Default radius for repel blocks */
    public int          repelRadius       = 16;

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
    public double moveVolumeEffect = 1;

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
    public List<String>  mystLocs              = Lists.newArrayList();
    @Configure(category = Config.advanced)
    boolean              reputs                = false;
    @Configure(category = Config.advanced)
    // DOLATER find more internal variables to add to this.
    public List<String>  extraVars             = Lists.newArrayList(new String[] { "jc:" + EventsHandler.juiceChance,
            "rc:" + EventsHandler.candyChance, "eggDpl:" + ItemPokemobEgg.PLAYERDIST, "eggDpm:"
                    + ItemPokemobEgg.MOBDIST });
    @Configure(category = Config.advanced)
    public boolean       debug                 = false;
    @Configure(category = Config.advanced)
    public List<String>  damageBlocksWhitelist = Lists.newArrayList(new String[] { "flash", "teleport", "dig", "cut",
            "rocksmash", "secretpower", "naturepower" });
    @Configure(category = Config.advanced)
    public List<String>  damageBlocksBlacklist = Lists.newArrayList();
    @Configure(category = Config.advanced)
    public String        nonPokemobExpFunction = "h*(a+1)^2";
    @Configure(category = Config.advanced)
    public boolean       nonPokemobExp         = false;
    @Configure(category = Config.advanced)
    public List<Integer> teleDimBlackList      = Lists.newArrayList();

    @Configure(category = Config.advanced)
    public boolean pokemobsAreAllFrozen = false;

    @Configure(category = Config.advanced)
    public int pokecubeAutoSendOutDelay = 20;

    @Configure(category = Config.genetics)
    public String       epigeneticEVFunction = GeneticsManager.epigeneticFunction;
    @Configure(category = Config.genetics)
    public List<String> mutationRates        = GeneticsManager.getMutationConfig();

    @Configure(category = Config.database)
    public boolean forceBerries = true;
    @Configure(category = Config.database)
    public boolean useCache     = true;

    @Configure(category = Config.database)
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

    @Configure(category = Config.mobAI, type = Type.SERVER)
    public boolean wildDeadDespawn = true;
    @Configure(category = Config.mobAI, type = Type.SERVER)
    public boolean tameDeadDespawn = true;

    @Configure(category = Config.mobAI, type = Type.SERVER)
    public int deadDespawnTimer = 20;
    @Configure(category = Config.mobAI, type = Type.SERVER)
    public int deadReviveTimer  = 600;

    // ridden Speed multipliers
    @Configure(category = Config.mobAI, type = Type.SERVER)
    public double flySpeedFactor    = 1;
    @Configure(category = Config.mobAI, type = Type.SERVER)
    public double surfSpeedFactor   = 1;
    @Configure(category = Config.mobAI, type = Type.SERVER)
    public double groundSpeedFactor = 1;

    @Configure(category = Config.mobAI, type = Type.SERVER)
    public List<String> blackListedFlyDims = Lists.newArrayList(new String[] { "the_end", "the_nether" });

    @Configure(category = Config.mobAI, type = Type.SERVER)
    public int attackCooldown = 20; // Synced so that the cooldown bars match

    @Configure(category = Config.spawning, type = Type.SERVER)
    /**
     * This is also the radius which mobs spawn in. Is only despawn radius if
     * cull is true
     */
    public int maxSpawnRadius = 32; // Synced for pokewatch
    @Configure(category = Config.spawning, type = Type.SERVER)
    /** closest distance to a player the pokemob can spawn. */
    public int minSpawnRadius = 16; // Synced for pokewatch

    @Configure(category = Config.misc, type = Type.SERVER)
    public boolean npcNameTags = true; // Synced for balance reasons

    @Configure(category = Config.misc, type = Type.SERVER)
    public double scalefactor = 1;

    @Configure(category = Config.misc, type = Type.SERVER)
    public int pcPageCount = 32;

    @Configure(category = Config.advanced, type = Type.SERVER)
    public int evolutionTicks = 50;
    @Configure(category = Config.advanced, type = Type.SERVER)
    public int baseRadarRange = 64;

    @Configure(category = Config.advanced, type = Type.SERVER)
    public int telePearlsCostSameDim  = 0;
    @Configure(category = Config.advanced, type = Type.SERVER)
    public int telePearlsCostOtherDim = 16;

    @Configure(category = Config.advanced, type = Type.SERVER)
    public double largeMobForSplit = 2;

    @Configure(category = Config.advanced, type = Type.SERVER)
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

        // TODO Init secret bases.
        // DimensionSecretBase.init(baseSizeFunction);
        PokecubeTerrainChecker.initStructMap();

        SpawnHandler.MAX_DENSITY = this.mobDensityMultiplier;
        SpawnHandler.MAXNUM = this.mobSpawnNumber;
        if (this.breedingDelay < 600) this.breedingDelay = 1000;

        SpawnHandler.doSpawns = this.pokemonSpawn;
        SpawnHandler.lvlCap = this.shouldCap;
        SpawnHandler.capLevel = this.levelCap;
        SpawnHandler.expFunction = this.expFunction;
        SpawnHandler.initSpawnFunctions();
        SpawnHandler.refreshSubbiomes = this.refreshSubbiomes;
        SpawnHandler.DEFAULT_VARIANCE = new FunctionVariance(this.spawnLevelVariance);

        PokecubeSerializer.MeteorDistance = this.meteorDistance * this.meteorDistance;
        PokecubeMod.debug = this.debug;
        for (final String loc : this.mystLocs)
            PokecubeMod.giftLocations.add(loc);
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
        for (final String s : this.snag_cube_blacklist)
            Pokecube.snagblacklist.add(new ResourceLocation(s));

        PokecubeItems.resetTimeTags = this.reputs;

        if (this.configDatabases.size() != EnumDatabase.values().length) this.configDatabases = Lists.newArrayList(
                new String[] { "", "", "" });

        SpawnHandler.dimensionBlacklist.clear();
        for (final String i : this.spawnDimBlacklist)
        {
            final DimensionType type = DimensionType.byName(new ResourceLocation(i));
            if (type != null) SpawnHandler.dimensionBlacklist.add(type);
        }
        SpawnHandler.dimensionWhitelist.clear();
        for (final String i : this.spawnDimWhitelist)
        {
            final DimensionType type = DimensionType.byName(new ResourceLocation(i));
            if (type != null) SpawnHandler.dimensionWhitelist.add(type);
        }
        LogicMountedControl.BLACKLISTED.clear();
        for (final String i : this.blackListedFlyDims)
        {
            final DimensionType type = DimensionType.byName(new ResourceLocation(i));
            if (type != null) LogicMountedControl.BLACKLISTED.add(type);
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
