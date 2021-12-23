package thut.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import thut.core.common.ThutCore;

public class Tracker
{
    private static Tracker INSTANCE = new Tracker();

    public static Tracker instance()
    {
        return Tracker.INSTANCE;
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(Tracker::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(Tracker::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(Tracker::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(Tracker::onWorldSave);
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
            Tracker.read(tag);
        }
        catch (final IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void onWorldSave(final WorldEvent.Save event)
    {
        if (!(event.getWorld() instanceof ServerLevel)) return;
        final ServerLevel world = (ServerLevel) event.getWorld();
        if (world.dimension() != Level.OVERWORLD) return;

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

    public static void read(final CompoundTag nbt)
    {
        Tracker.instance().time = nbt.getLong("tick_timer");
    }

    public static CompoundTag write()
    {
        final CompoundTag tag = new CompoundTag();
        tag.putLong("tick_timer", Tracker.instance().time);
        return tag;
    }
}
