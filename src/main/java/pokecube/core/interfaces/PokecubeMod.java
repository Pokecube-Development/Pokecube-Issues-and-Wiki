package pokecube.core.interfaces;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.loading.FMLPaths;

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

    private static HashMap<World, FakePlayer> fakePlayers = new HashMap<>();

    public static final UUID fakeUUID = new UUID(1234, 4321);

    public static Logger  LOGGER = null;
    public static boolean debug;

    private static FakePlayer makeNewFakePlayer(final ServerWorld world)
    {
        return FakePlayerFactory.get(world, new GameProfile(PokecubeMod.fakeUUID, "[Pokecube]DispenserPlayer"));
    }

    public static FakePlayer getFakePlayer(final World world)
    {
        if (!(world instanceof ServerWorld)) throw new IllegalArgumentException("Must be called server side!");
        return PokecubeMod.getFakePlayer((ServerWorld) world);
    }

    public static FakePlayer getFakePlayer(final ServerWorld world)
    {
        final FakePlayer player = PokecubeMod.fakePlayers.getOrDefault(world, PokecubeMod.makeNewFakePlayer(world));
        PokecubeMod.fakePlayers.put(world, player);
        player.setLevel(world);
        return player;
    }
    public static void setLogger(final Logger logger_in)
    {
        PokecubeMod.LOGGER = logger_in;
        final String log = PokecubeMod.ID;
        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(log + ".log").toFile();
        if (logfile.exists())
        {
            FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").toFile().mkdirs();
            try
            {
                final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                Files.move(FMLPaths.GAMEDIR.get().resolve("logs").resolve(log + ".log"), FMLPaths.GAMEDIR.get().resolve(
                        "logs").resolve("old").resolve(String.format("%s_%s%s", log, LocalDateTime.now().format(dtf)
                                .replace(":", "-"), ".log")));
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) logger_in;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                PokecubeMod.ID).build();
        logger.addAppender(appender);
        appender.start();
    }
}
