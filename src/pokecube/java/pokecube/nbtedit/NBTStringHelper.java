package pokecube.nbtedit;

import com.google.common.base.Strings;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import pokecube.nbtedit.nbt.NamedNBT;

public class NBTStringHelper
{
    public static final char SECTION_SIGN = '\u00A7';

    public static String getButtonName(final byte id)
    {
        switch (id)
        {
        case 1:
            return "Byte";
        case 2:
            return "Short";
        case 3:
            return "Int";
        case 4:
            return "Long";
        case 5:
            return "Float";
        case 6:
            return "Double";
        case 7:
            return "Byte[]";
        case 8:
            return "String";
        case 9:
            return "List";
        case 10:
            return "Compound";
        case 11:
            return "Int[]";
        case 12:
            return "Edit";
        case 13:
            return "Delete";
        case 14:
            return "Copy";
        case 15:
            return "Cut";
        case 16:
            return "Paste";
        default:
            return "Unknown";
        }
    }

    public static String getNBTName(final NamedNBT namedNBT)
    {
        final String name = namedNBT.getName();
        final Tag obj = namedNBT.getNBT();

        final String s = NBTStringHelper.toString(obj);
        return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s;
    }

    public static String getNBTNameSpecial(final NamedNBT namedNBT)
    {
        final String name = namedNBT.getName();
        final Tag obj = namedNBT.getNBT();

        final String s = NBTStringHelper.toString(obj);
        return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s + NBTStringHelper.SECTION_SIGN + 'r';
    }

    public static Tag newTag(final byte type)
    {
        switch (type)
        {
        case 0:
            return EndTag.INSTANCE;
        case 1:
            return ByteTag.valueOf((byte) 0);
        case 2:
            return ShortTag.valueOf((short) 0);
        case 3:
            return IntTag.valueOf(0);
        case 4:
            return LongTag.valueOf(0);
        case 5:
            return FloatTag.valueOf(0);
        case 6:
            return DoubleTag.valueOf(0);
        case 7:
            return new ByteArrayTag(new byte[0]);
        case 8:
            return StringTag.valueOf("");
        case 9:
            return new ListTag();
        case 10:
            return new CompoundTag();
        case 11:
            return new IntArrayTag(new int[0]);
        default:
            return null;
        }
    }

    public static String toString(final Tag base)
    {
        switch (base.getId())
        {
        case 1:
            return "" + ((ByteTag) base).getAsByte();
        case 2:
            return "" + ((ShortTag) base).getAsShort();
        case 3:
            return "" + ((IntTag) base).getAsInt();
        case 4:
            return "" + ((LongTag) base).getAsLong();
        case 5:
            return "" + ((FloatTag) base).getAsFloat();
        case 6:
            return "" + ((DoubleTag) base).getAsDouble();
        case 7:
            return base.toString();
        case 8:
            return ((StringTag) base).getAsString();
        case 9:
            return "(TagList)";
        case 10:
            return "(TagCompound)";
        case 11:
            return base.toString();
        default:
            return "?";
        }
    }
}
