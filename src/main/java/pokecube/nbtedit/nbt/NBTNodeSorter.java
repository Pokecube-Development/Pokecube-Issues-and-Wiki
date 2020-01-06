package pokecube.nbtedit.nbt;

import java.util.Comparator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

public class NBTNodeSorter implements Comparator<Node<NamedNBT>>
{

    @Override
    public int compare(Node<NamedNBT> a, Node<NamedNBT> b)
    {
        final INBT n1 = a.getObject().getNBT(), n2 = b.getObject().getNBT();
        final String s1 = a.getObject().getName(), s2 = b.getObject().getName();
        if (n1 instanceof CompoundNBT || n1 instanceof ListNBT)
        {
            if (n2 instanceof CompoundNBT || n2 instanceof ListNBT)
            {
                final int dif = n1.getId() - n2.getId();
                return dif == 0 ? s1.compareTo(s2) : dif;
            }
            return 1;
        }
        if (n2 instanceof CompoundNBT || n2 instanceof ListNBT) return -1;
        final int dif = n1.getId() - n2.getId();
        return dif == 0 ? s1.compareTo(s2) : dif;
    }

}
