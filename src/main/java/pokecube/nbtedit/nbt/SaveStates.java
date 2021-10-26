package pokecube.nbtedit.nbt;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pokecube.nbtedit.NBTEdit;

// This save format can definitely be improved. Also, this can be extended to
// provide infinite save slots - just
// need to add some scrollbar (use GuiLib!).
public class SaveStates
{

    public static final class SaveState
    {
        public String      name;
        public CompoundNBT tag;

        public SaveState(String name)
        {
            this.name = name;
            this.tag = new CompoundNBT();
        }
    }

    private final File file;

    private final SaveState[] tags;

    public SaveStates(File file)
    {
        this.file = file;
        this.tags = new SaveState[7];
        for (int i = 0; i < 7; ++i)
            this.tags[i] = new SaveState("Slot " + (i + 1));
    }

    public SaveState getSaveState(int index)
    {
        return this.tags[index];
    }

    public void load()
    {
        try
        {
            this.read();
            NBTEdit.log(Level.TRACE, "NBTEdit save loaded successfully.");
        }
        catch (final IOException e)
        {
            NBTEdit.log(Level.WARN, "Unable to read NBTEdit save.");
            NBTEdit.throwing("SaveStates", "load", e);
        }
    }

    public void read() throws IOException
    {
        if (this.file.exists() && this.file.canRead())
        {
            final CompoundNBT root = CompressedStreamTools.read(this.file);
            for (int i = 0; i < 7; ++i)
            {
                final String name = "slot" + (i + 1);
                if (root.contains(name)) this.tags[i].tag = root.getCompound(name);
                if (root.contains(name + "Name")) this.tags[i].name = root.getString(name + "Name");
            }
        }
    }

    public void save()
    {
        try
        {
            this.write();
            NBTEdit.log(Level.TRACE, "NBTEdit saved successfully.");
        }
        catch (final IOException e)
        {
            NBTEdit.log(Level.WARN, "Unable to write NBTEdit save.");
            NBTEdit.throwing("SaveStates", "save", e);
        }
    }

    public void write() throws IOException
    {
        final CompoundNBT root = new CompoundNBT();
        for (int i = 0; i < 7; ++i)
        {
            root.put("slot" + (i + 1), this.tags[i].tag);
            root.putString("slot" + (i + 1) + "Name", this.tags[i].name);
        }
        CompressedStreamTools.write(root, this.file);
    }
}
