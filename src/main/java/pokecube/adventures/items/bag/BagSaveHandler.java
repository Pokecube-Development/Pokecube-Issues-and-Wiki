package pokecube.adventures.items.bag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import pokecube.core.PokecubeCore;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

public class BagSaveHandler
{
    private static BagSaveHandler instance;

    private static BagSaveHandler clientInstance;

    public static BagSaveHandler getInstance()
    {
        if (ThutCore.proxy.isServerSide())
        {
            if (BagSaveHandler.instance == null) BagSaveHandler.instance = new BagSaveHandler();
            return BagSaveHandler.instance;
        }
        if (BagSaveHandler.clientInstance == null) BagSaveHandler.clientInstance = new BagSaveHandler();
        return BagSaveHandler.clientInstance;
    }

    public boolean seenPCCreator = false;

    public BagSaveHandler()
    {
    }

    public void loadPC(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "BagInventory");
            if (file != null && file.exists())
            {
                PokecubeCore.LOGGER.debug("Loading PC: " + uuid);
                final FileInputStream fileinputstream = new FileInputStream(file);
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                this.readPcFromNBT(CompoundNBT.getCompound("Data"));
            }
        }
        catch (final FileNotFoundException e)
        {
        }
        catch (final Exception e)
        {
        }
    }

    public void readPcFromNBT(final CompoundNBT nbt)
    {
        this.seenPCCreator = nbt.getBoolean("seenPCCreator");
        // Read PC Data from NBT
        final INBT temp = nbt.get("PC");
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListPC = (ListNBT) temp;
            BagInventory.loadFromNBT(tagListPC);
        }
    }

    public void savePC(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "BagInventory");
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                this.writePcToNBT(CompoundNBT, uuid);
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

    public void writePcToNBT(final CompoundNBT nbt, final UUID uuid)
    {
        nbt.putBoolean("seenPCCreator", this.seenPCCreator);
        final ListNBT tagsPC = BagInventory.saveToNBT(uuid);
        nbt.put("PC", tagsPC);
    }

}
