package pokecube.nbtedit;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTHelper
{
    public static Tag getTagAt(final ListTag tag, final int index)
    {
        return tag.get(index);
    }
}
