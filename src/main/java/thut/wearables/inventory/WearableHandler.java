package thut.wearables.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import thut.wearables.ThutWearables;

public class WearableHandler
{
    @CapabilityInject(IWearableInventory.class)
    public static final Capability<IWearableInventory> WEARABLES_CAP = null;

    private static File getFileForUUID(final String uuid, final String fileName)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        Path path = server.func_240776_a_(new FolderName("wearables"));
        // This is to the uuid specific folder
        path = path.resolve("uuid");
        final File dir = path.toFile();
        // and this if the file itself
        path = path.resolve(fileName + ".dat");
        final File file = path.toFile();
        if (!file.exists()) dir.mkdirs();
        if (!file.exists()) return null;
        return file;
    }

    public static PlayerWearables getPlayerData(final String uuid)
    {
        return WearableHandler.load(uuid);
    }

    public static PlayerWearables load(final String uuid)
    {
        if (ThutWearables.proxy.isServerSide())
        {
            final PlayerWearables wearables = new PlayerWearables();
            final String fileName = wearables.dataFileName();
            File file = null;
            try
            {
                file = WearableHandler.getFileForUUID(uuid, fileName);
            }
            catch (final Exception e)
            {

            }
            if (file != null && file.exists()) try
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                wearables.readFromNBT(CompoundNBT.getCompound("Data"));
                // Cleanup the file, we don't need tihs anymore.
                file.delete();
                final File dir = new File(file.getParentFile().getAbsolutePath());
                if (dir.isDirectory() && dir.listFiles().length == 0) dir.delete();
                return wearables;
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
}
