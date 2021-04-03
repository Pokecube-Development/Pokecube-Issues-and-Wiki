package thut.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

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

    // Load the time and set it.
    private static void onServerStart(final FMLServerStartedEvent event)
    {
        final MinecraftServer server = event.getServer();
        Path path = server.getWorldPath(new FolderName("thutcore"));
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
            final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
            fileinputstream.close();
            final CompoundNBT tag = CompoundNBT.getCompound("Data");
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
        if (!(event.getWorld() instanceof ServerWorld)) return;
        final ServerWorld world = (ServerWorld) event.getWorld();
        if (world.dimension() != World.OVERWORLD) return;

        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        Path path = server.getWorldPath(new FolderName("thutcore"));
        final File dir = path.toFile();
        // and this if the file itself
        path = path.resolve("worlddata.dat");
        final File file = path.toFile();
        if (!file.exists()) dir.mkdirs();

        final CompoundNBT tag = Tracker.write();
        final CompoundNBT CompoundNBT1 = new CompoundNBT();
        CompoundNBT1.put("Data", tag);
        try
        {
            final FileOutputStream fileoutputstream = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
            fileoutputstream.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void read(final CompoundNBT nbt)
    {
        Tracker.instance().time = nbt.getLong("tick_timer");
    }

    public static CompoundNBT write()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putLong("tick_timer", Tracker.instance().time);
        return tag;
    }
}
