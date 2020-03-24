package pokecube.core.inventory.pc;

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

public class PCSaveHandler
{
    private static PCSaveHandler instance;

    private static PCSaveHandler clientInstance;

    public static PCSaveHandler getInstance()
    {
        if (ThutCore.proxy.isServerSide())
        {
            if (PCSaveHandler.instance == null) PCSaveHandler.instance = new PCSaveHandler();
            return PCSaveHandler.instance;
        }
        if (PCSaveHandler.clientInstance == null) PCSaveHandler.clientInstance = new PCSaveHandler();
        return PCSaveHandler.clientInstance;
    }

    public boolean seenPCCreator = false;

    public PCSaveHandler()
    {
    }

    public void load(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "PCInventory");
            if (file != null && file.exists())
            {
                PokecubeCore.LOGGER.debug("Loading PC: " + uuid);
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

    public void loadNBT(final CompoundNBT nbt)
    {
        this.seenPCCreator = nbt.getBoolean("seenPCCreator");
        // Read PC Data from NBT
        final INBT temp = nbt.get("PC");
        if (temp instanceof ListNBT)
        {
            final ListNBT tagListPC = (ListNBT) temp;
            PCInventory.loadFromNBT(tagListPC);
        }
    }

    public void save(final UUID uuid)
    {
        if (ThutCore.proxy.isClientSide()) return;
        try
        {
            final File file = PlayerDataHandler.getFileForUUID(uuid.toString(), "PCInventory");
            if (file != null)
            {
                final CompoundNBT CompoundNBT = new CompoundNBT();
                this.writeToNBT(CompoundNBT, uuid);
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

    public void writeToNBT(final CompoundNBT nbt, final UUID uuid)
    {
        nbt.putBoolean("seenPCCreator", this.seenPCCreator);
        final ListNBT tagsPC = PCInventory.saveToNBT(uuid);
        nbt.put("PC", tagsPC);
    }

}
