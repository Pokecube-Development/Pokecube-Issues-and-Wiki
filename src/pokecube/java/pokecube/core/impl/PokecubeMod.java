package pokecube.core.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public abstract class PokecubeMod
{
    public final static String ID = "pokecube";

    // Copied from FakePlayerFactory.MINECRAFT
    public static final UUID fakeUUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");

    public static FakePlayer getFakePlayer(final Level world)
    {
        return getFakePlayer(world, null);
    }

    public static FakePlayer getFakePlayer(final Level world, UUID id)
    {
        if (!(world instanceof ServerLevel level)) throw new IllegalArgumentException("Must be called server side!");
        return PokecubeMod.getFakePlayer(level, id);
    }

    public static FakePlayer getFakePlayer(final ServerLevel world, UUID id)
    {
        if (id == null) return FakePlayerFactory.getMinecraft(world);
        return FakePlayerFactory.get(world, new GameProfile(id, "FakePlayer"));
    }

    public static void setLogger(final Logger logger_in)
    {
        final String log = PokecubeMod.ID;
        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(log + ".log").toFile();
        if (logfile.exists())
        {
            FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").toFile().mkdirs();
            try
            {
                final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                Files.move(FMLPaths.GAMEDIR.get().resolve("logs").resolve(log + ".log"),
                        FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").resolve(String.format("%s_%s%s", log,
                                LocalDateTime.now().format(dtf).replace(":", "-"), ".log")));
            }
            catch (final IOException e)
            {
                logger_in.error(e);
            }
        }
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) logger_in;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath())
                .setName(PokecubeMod.ID).build();
        logger.addAppender(appender);
        appender.start();
    }
}
