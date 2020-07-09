package thut.bling.bag.small;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import thut.api.inventory.big.Manager;
import thut.core.common.ThutCore;

public class SmallManager extends Manager<SmallInventory>
{
    public static SmallManager INSTANCE = new SmallManager();

    public static File getFileForUUID(final String uuid, final String fileName)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        final ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
        final SaveHandler saveHandler = world.getSaveHandler();
        final String seperator = System.getProperty("file.separator");
        final File worlddir = saveHandler.getWorldDirectory();
        final File file = new File(worlddir, "thut_bling" + seperator + uuid + ".dat");
        final File dir = new File(file.getParentFile().getAbsolutePath());
        if (!file.exists()) dir.mkdirs();
        return file;
    }

    public SmallManager()
    {
        super(s -> SmallContainer.isItemValid(s), SmallInventory::new, SmallInventory::new);
    }

    @Override
    public String fileName()
    {
        return "SmallEnderBag";
    }

    @Override
    public String tagID()
    {
        return "Bag";
    }

    @Override
    protected void save(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        final SmallInventory save = this.get(uuid, false);
        if (save == null || !save.dirty) return;
        try
        {
            final File file = SmallManager.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                this.writeToNBT(CompoundNBT, save);
                final CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.put("Data", CompoundNBT);
                final FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void load(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = SmallManager.getFileForUUID(uuid.toString(), this.fileName());
            if (file != null && file.exists())
            {
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                this.loadNBT(CompoundNBT.getCompound("Data"));
            }
        }
        catch (final FileNotFoundException e)
        {
        }
        catch (final Exception e)
        {
        }
    }

}
