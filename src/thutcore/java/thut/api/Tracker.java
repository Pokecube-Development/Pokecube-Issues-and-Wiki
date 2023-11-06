package thut.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import thut.core.common.ThutCore;

/**
 * Time tick tracker, Used for a global timer which does not reset with world
 * time, etc.
 *
 */
public class Tracker
{
    private static Tracker INSTANCE = new Tracker();

    public static Tracker instance()
    {
        return Tracker.INSTANCE;
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(Tracker::onServerTick);
        ThutCore.FORGE_BUS.addListener(Tracker::onClientTick);
        ThutCore.FORGE_BUS.addListener(Tracker::onServerStart);
        ThutCore.FORGE_BUS.addListener(Tracker::onWorldSave);
    }

    public static interface UpdateHandler
    {
        String getKey();

        void read(CompoundTag nbt, ServerPlayer player);
    }

    private static long start = System.nanoTime();
    private static long n = 0;
    private static long dt = 0;
    private static Object2LongArrayMap<String> taskCounts = new Object2LongArrayMap<>();
    private static Object2IntArrayMap<String> taskNs = new Object2IntArrayMap<>();
    public static Map<String, UpdateHandler> HANDLERS = new HashMap<>();

    public static void timerStart()
    {
        start = System.nanoTime();
    }

    public static void timerEnd(String involved, int reportRate)
    {
        long _dt = System.nanoTime() - start;
        dt += _dt;
        taskCounts.compute(involved, (key, value) -> {
            if (value == null) value = _dt;
            else value += _dt;
            return value;
        });
        taskNs.compute(involved, (key, value) -> {
            if (value == null) value = 1;
            else value += 1;
            return value;
        });
        n++;
        if (n >= reportRate)
        {
            double avg = dt / ((double) n);
            System.out.println("Average time: %.2f us".formatted((avg / 1000d)));
            System.out.println("key\ttime per\ttime total");
            taskCounts.forEach((clazz, val) -> {
                double avg2 = val / ((double) taskNs.getInt(clazz));
                String key = "%s\t%.2f\t%.2f";
                System.out.println(key.formatted(clazz, (avg2 / 1000d), (val / 1000d)));
            });
            taskCounts.clear();
            taskNs.clear();
            n = 0;
            dt = 0;
        }
    }

    long time = 0;

    public Tracker()
    {
        this.time = System.currentTimeMillis() / 50;
    }

    public long getTick()
    {
        return this.time;
    }

    // Increment time
    private static void onServerTick(final ServerTickEvent event)
    {
        if (event.phase == Phase.END) Tracker.instance().time++;
    }

    private static void onClientTick(final ClientTickEvent event)
    {
        // Force this to also increment client side while on a dedicated server.
        // This allows using the ticker for ensuring animations, etc keep
        // running as well.
        if (ServerLifecycleHooks.getCurrentServer() == null && event.phase == Phase.END) Tracker.instance().time++;
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
            final CompoundTag CompoundNBT = NbtIo.readCompressed(fileinputstream);
            fileinputstream.close();
            final CompoundTag tag = CompoundNBT.getCompound("Data");
            Tracker.read(tag, null);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void onWorldSave(final WorldEvent.Save event)
    {
        if (!(event.getWorld() instanceof ServerLevel level)) return;
        if (level.dimension() != Level.OVERWORLD) return;

        final MinecraftServer server = ThutCore.proxy.getServer();
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
            e.printStackTrace();
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
