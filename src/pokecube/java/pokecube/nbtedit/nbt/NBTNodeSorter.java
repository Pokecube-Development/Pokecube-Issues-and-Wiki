package pokecube.nbtedit.nbt;

import java.util.Comparator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTNodeSorter implements Comparator<Node<NamedNBT>>
{

    @Override
    public int compare(Node<NamedNBT> a, Node<NamedNBT> b)
    {
        final Tag n1 = a.getObject().getNBT(), n2 = b.getObject().getNBT();
        final String s1 = a.getObject().getName(), s2 = b.getObject().getName();
        if (n1 instanceof CompoundTag || n1 instanceof ListTag)
        {
            if (n2 instanceof CompoundTag || n2 instanceof ListTag)
            {
                final int dif = n1.getId() - n2.getId();
                return dif == 0 ? s1.compareTo(s2) : dif;
            }
            return 1;
        }
        if (n2 instanceof CompoundTag || n2 instanceof ListTag) return -1;
        final int dif = n1.getId() - n2.getId();
        return dif == 0 ? s1.compareTo(s2) : dif;
    }

}
