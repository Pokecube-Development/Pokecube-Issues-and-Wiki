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
    public static final char SECTION_SIGN = '§';

    public static final int BUTTON_PASTE = 16;
    public static final int BUTTON_CUT = 15;
    public static final int BUTTON_COPY = 14;

    public static final int BUTTON_EDIT = 12;
    public static final int BUTTON_DEL = 13;

    public static String getButtonName(int id)
    {
        return switch (id)
        {
            case 1 -> "Byte";
            case 2 -> "Short";
            case 3 -> "Int";
            case 4 -> "Long";
            case 5 -> "Float";
            case 6 -> "Double";
            case 7 -> "Byte[]";
            case 8 -> "String";
            case 9 -> "List";
            case 10 -> "Compound";
            case 11 -> "Int[]";
            case 12 -> "Edit";
            case 13 -> "Delete";
            case 14 -> "Copy";
            case 15 -> "Cut";
            case 16 -> "Paste";
            default -> "Unknown";
        };
    }

    public static String getNBTName(final NamedNBT namedNBT)
    {
        final String name = namedNBT.getName();
        final Tag obj = namedNBT.getNBT();

        final String s = NBTStringHelper.toString(obj);
        return Strings.isNullOrEmpty(name) ? s : name + ": " + s;
    }

    public static String getNBTNameSpecial(final NamedNBT namedNBT)
    {
        final String name = namedNBT.getName();
        final Tag obj = namedNBT.getNBT();

        final String s = NBTStringHelper.toString(obj);
        return Strings.isNullOrEmpty(name) ? s : name + ": " + s + NBTStringHelper.SECTION_SIGN + 'r';
    }

    public static Tag newTag(final int type)
    {
        return switch (type)
        {
            case 0 -> EndTag.INSTANCE;
            case 1 -> ByteTag.valueOf((byte) 0);
            case 2 -> ShortTag.valueOf((short) 0);
            case 3 -> IntTag.valueOf(0);
            case 4 -> LongTag.valueOf(0);
            case 5 -> FloatTag.valueOf(0);
            case 6 -> DoubleTag.valueOf(0);
            case 7 -> new ByteArrayTag(new byte[0]);
            case 8 -> StringTag.valueOf("");
            case 9 -> new ListTag();
            case 10 -> new CompoundTag();
            case 11 -> new IntArrayTag(new int[0]);
            default -> null;
        };
    }

    public static String toString(final Tag base)
    {
        return switch (base.getId())
        {
            case 1 -> "" + ((ByteTag) base).getAsByte();
            case 2 -> "" + ((ShortTag) base).getAsShort();
            case 3 -> "" + ((IntTag) base).getAsInt();
            case 4 -> "" + ((LongTag) base).getAsLong();
            case 5 -> "" + ((FloatTag) base).getAsFloat();
            case 6 -> "" + ((DoubleTag) base).getAsDouble();
            case 7, 11 -> base.toString();
            case 8 -> base.getAsString();
            case 9 -> "(TagList)";
            case 10 -> "(TagCompound)";
            default -> "?";
        };
    }
}
