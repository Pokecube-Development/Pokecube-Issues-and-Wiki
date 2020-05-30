package pokecube.core.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class PokecubeMod
{

    public final static String ID              = "pokecube";
    public final static String VERSION         = "@VERSION";
    public final static String MCVERSIONS      = "@MCVERSION";
    public final static String MINVERSION      = "@MINVERSION";
    public final static String MINFORGEVERSION = "@FORGEVERSION";

    public final static String DEPSTRING = ";required-after:thutcore@@THUTCORE";
    public final static String GIST      = "https://gist.githubusercontent.com/Thutmose/4d7320c36696cd39b336/raw/";
    public final static String UPDATEURL = "https://raw.githubusercontent.com/Pokecube-Development/Pokecube-Core/master/versions.json";

    public final static String GIFTURL = PokecubeMod.GIST + "gift";

    private static HashMap<DimensionType, FakePlayer> fakePlayers = new HashMap<>();

    public static double              MAX_DENSITY   = 1;
    public static Map<String, String> gifts         = Maps.newHashMap();
    public static Set<String>         giftLocations = Sets.newHashSet();

    public static final UUID fakeUUID = new UUID(1234, 4321);

    public static Logger  LOGGER = null;
    public static boolean debug;

    public static FakePlayer getFakePlayer(final DimensionType dim)
    {
        if (PokecubeMod.fakePlayers.get(dim) == null)
        {
            ServerWorld world = null;

            if (ThutCore.proxy.isServerSide())
            {
                final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
                world = server.getWorld(dim);
            }

            final FakePlayer fakeplayer = FakePlayerFactory.get(world, new GameProfile(PokecubeMod.fakeUUID,
                    "[Pokecube]DispenserPlayer"));
            PokecubeMod.fakePlayers.put(dim, fakeplayer);
        }
        return PokecubeMod.fakePlayers.get(dim);
    }

    public static FakePlayer getFakePlayer(final World world)
    {
        final FakePlayer player = PokecubeMod.getFakePlayer(world.dimension.getDimension().getType());
        player.setWorld(world);
        return player;
    }

    public ArrayList<PokedexEntry> starters = new ArrayList<>();

    /**
     * Creates a new instance of an entity in the world for the pokemob
     * specified by its pokedex entry.
     *
     * @param entry
     *            the pokedexentry
     * @param world
     *            the {@link World} where to spawn
     * @return the {@link Entity} instance or null if a problem occurred
     */
    public abstract Entity createPokemob(PokedexEntry entry, World world);

    public abstract Config getConfig();

    /**
     * Returns the class of the {@link MobEntity} for the given pokedexNb. If
     * no Pokemob has been registered for this pokedex number, it returns
     * <code>null</code>.
     *
     * @param pokedexNb
     *            the pokedex number
     * @return the {@link Class} of the pokemob
     */
    @SuppressWarnings("rawtypes")
    public abstract Class getEntityClassForEntry(PokedexEntry entry);

    public abstract IEntityProvider getEntityProvider();

    public abstract PokedexEntry[] getStarters();

    /**
     * Registers a Pokemob into the Pokedex. Have a look to the file called
     * <code>"HelpEntityJava.png"</code> provided with the SDK.
     *
     * @param createEgg
     *            whether an egg should be created for this species (is a base
     *            non legendary pokemob)
     * @param mod
     *            the instance of your mod
     * @param entry
     *            the pokedex entry
     */
    public abstract void registerPokemon(boolean createEgg, Object mod, PokedexEntry entry);

    @SuppressWarnings("rawtypes")
    public abstract void registerPokemonByClass(Class clazz, boolean createEgg, Object mod, PokedexEntry entry);

    public abstract void setEntityProvider(IEntityProvider provider);

    public static void setLogger(final Logger logger_in)
    {
        PokecubeMod.LOGGER = logger_in;
        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(PokecubeMod.ID + ".log").toFile();
        if (logfile.exists()) logfile.delete();
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) logger_in;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                PokecubeMod.ID).build();
        logger.addAppender(appender);
        appender.start();
    }

    public abstract void spawnParticle(World world, String par1Str, Vector3 location, Vector3 velocity, int... args);
}
