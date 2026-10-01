package thut.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Time tick tracker, Used for a global timer which does not reset with world
 * time, etc.
 *
 */
public class Tracker
{
    private static final Tracker INSTANCE = new Tracker();

    public static Tracker instance()
    {
        return Tracker.INSTANCE;
    }

    public static void init()
    {
        NeoForge.EVENT_BUS.addListener(Tracker::onServerTick);
        NeoForge.EVENT_BUS.addListener(Tracker::onClientTick);
        NeoForge.EVENT_BUS.addListener(Tracker::onServerStart);
        NeoForge.EVENT_BUS.addListener(Tracker::onWorldSave);
    }

    public static interface UpdateHandler
    {
        String getKey();

        void read(CompoundTag nbt, ServerPlayer player);
    }

    public static class Counter
    {
        public final String key;
        public final int reportRate;
        private final AtomicInteger N = new AtomicInteger();
        private long lastReport;

        public Counter(String key, int reportRate)
        {
            this.key = key;
            this.reportRate = reportRate;
            lastReport = Tracker.instance().getTick();
            N.set(0);
        }

        public void increment()
        {
            N.incrementAndGet();
        }

        public int report()
        {
            long tick = Tracker.instance().getTick();
            long dN = tick - lastReport;
            if (dN >= reportRate)
            {
                lastReport = tick;
                return N.getAndSet(0);
            }
            return -1;
        }
    }

    public static Map<String, UpdateHandler> HANDLERS = new HashMap<>();
    public static Map<String, Counter> CLIENT_COUNTERS = new HashMap<>();
    public static Map<String, Counter> SERVER_COUNTERS = new HashMap<>();

    long time;

    public Tracker()
    {
        this.time = System.currentTimeMillis() / 50;
    }

    public long getTick()
    {
        return this.time;
    }

    // Increment time
    private static void onServerTick(final ServerTickEvent.Post event)
    {
        Tracker.instance().time++;
        SERVER_COUNTERS.values().forEach(counter -> {
            int n = counter.report();
            if (n > 0)
            {
                int toSeconds = counter.reportRate / 20;
                if (toSeconds > 0 && counter.reportRate % 20 == 0) System.out.println(
                        "Counter: " + counter.key + ", Rate: " + n + "/" + counter.reportRate + " (" + (n / toSeconds)
                                + "/s)");
                else System.out.println("Counter: " + counter.key + ", Rate: " + n + "/" + counter.reportRate);
            }
        });
    }

    private static void onClientTick(final ClientTickEvent.Post event)
    {
        // Force this to also increment client side while on a dedicated server.
        // This allows using the ticker for ensuring animations, etc keep
        // running as well.
        if (ServerLifecycleHooks.getCurrentServer() == null) Tracker.instance().time++;
        CLIENT_COUNTERS.values().forEach(counter -> {
            int n = counter.report();
            if (n >= 0)
            {
                System.out.println("Counter: " + counter.key + ", Rate: " + n + "/" + counter.reportRate);
            }
        });
    }

    // Load the time and set it.
    private static void onServerStart(final ServerStartedEvent event)
    {
        final MinecraftServer server = event.getServer();
        Path path = server.getWorldPath(new LevelResource("thutcore"));
        final File dir = path.toFile();
        // and this if the file itself
        path = path.resolve("worlddata.dat");
        final File file = path.toFile();
        if (!file.exists())
        {
            dir.mkdirs();
            return;
        }
        try
        {
            final FileInputStream fileinputstream = new FileInputStream(file);
            final CompoundTag CompoundNBT = NbtIo.readCompressed(fileinputstream, NbtAccounter.create(104857600L));
            fileinputstream.close();
            final CompoundTag tag = CompoundNBT.getCompound("Data");
            Tracker.read(tag, null);
        }
        catch (final IOException e)
        {
            ThutAPI.LOGGER.error(e);
        }
    }

    private static void onWorldSave(final LevelEvent.Save event)
    {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Path path = server.getWorldPath(new LevelResource("thutcore"));
        final File dir = path.toFile();
        // and this if the file itself
        path = path.resolve("worlddata.dat");
        final File file = path.toFile();
        if (!file.exists()) dir.mkdirs();

        final CompoundTag tag = Tracker.write();
        final CompoundTag CompoundNBT1 = new CompoundTag();
        CompoundNBT1.put("Data", tag);
        try
        {
            final FileOutputStream fileoutputstream = new FileOutputStream(file);
            NbtIo.writeCompressed(CompoundNBT1, fileoutputstream);
            fileoutputstream.close();
        }
        catch (final IOException e)
        {
            ThutAPI.LOGGER.error(e);
        }
    }

    public static void read(final CompoundTag nbt, ServerPlayer player)
    {
        if (nbt.contains("key"))
        {
            String key = nbt.getString("key");
            CompoundTag tag = nbt.getCompound("tag");
            var handler = HANDLERS.get(key);
            if (handler != null) handler.read(tag, player);
        }
        else if (player == null) Tracker.instance().time = nbt.getLong("tick_timer");
    }

    public static CompoundTag write()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putLong("tick_timer", Tracker.instance().time);
        return tag;
    }
}
