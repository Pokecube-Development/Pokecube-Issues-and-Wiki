package thut.core.common.network;

import java.util.Collection;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * This code is taken from
 * https://github.com/McJtyMods/TheOneProbe/blob/fdbfc674a118ee2a880d67276a83e4b2ba197bf9/src/main/java/mcjty/theoneprobe/network/NetworkTools.java
 *
 * @author McJty
 */
public class NetworkTools
{

    public static <T extends Enum<T>> T readEnum(ByteBuf buf, T[] values)
    {
        return values[buf.readInt()];
    }

    public static <T extends Enum<T>> void readEnumCollection(ByteBuf buf, Collection<T> collection, T[] values)
    {
        collection.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
            collection.add(values[buf.readInt()]);
    }

    public static Float readFloat(ByteBuf buf)
    {
        if (buf.readBoolean()) return buf.readFloat();
        else return null;
    }

    /// This function supports itemstacks with more then 64 items.
    public static ItemStack readItemStack(ByteBuf dataIn)
    {
        final PacketBuffer buf = new PacketBuffer(dataIn);
        final CompoundNBT nbt = buf.readNbt();
        final ItemStack stack = ItemStack.of(nbt);
        stack.setCount(buf.readInt());
        return stack;
    }

    public static CompoundNBT readNBT(ByteBuf dataIn)
    {
        final PacketBuffer buf = new PacketBuffer(dataIn);
        return buf.readNbt();
    }

    public static BlockPos readPos(ByteBuf dataIn)
    {
        return new BlockPos(dataIn.readInt(), dataIn.readInt(), dataIn.readInt());
    }

    public static String readString(ByteBuf dataIn)
    {
        final int s = dataIn.readInt();
        if (s == -1) return null;
        if (s == 0) return "";
        final byte[] dst = new byte[s];
        dataIn.readBytes(dst);
        return new String(dst);
    }

    public static String readStringUTF8(ByteBuf dataIn)
    {
        final int s = dataIn.readInt();
        if (s == -1) return null;
        if (s == 0) return "";
        final byte[] dst = new byte[s];
        dataIn.readBytes(dst);
        return new String(dst, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static <T extends Enum<T>> void writeEnum(ByteBuf buf, T value, T nullValue)
    {
        if (value == null) buf.writeInt(nullValue.ordinal());
        else buf.writeInt(value.ordinal());
    }

    public static <T extends Enum<T>> void writeEnumCollection(ByteBuf buf, Collection<T> collection)
    {
        buf.writeInt(collection.size());
        for (final T type : collection)
            buf.writeInt(type.ordinal());
    }

    public static void writeFloat(ByteBuf buf, Float f)
    {
        if (f != null)
        {
            buf.writeBoolean(true);
            buf.writeFloat(f);
        }
        else buf.writeBoolean(false);
    }

    /// This function supports itemstacks with more then 64 items.
    public static void writeItemStack(ByteBuf dataOut, ItemStack itemStack)
    {
        final PacketBuffer buf = new PacketBuffer(dataOut);
        final CompoundNBT nbt = new CompoundNBT();
        itemStack.save(nbt);
        try
        {
            buf.writeNbt(nbt);
            buf.writeInt(itemStack.getCount());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void writeNBT(ByteBuf dataOut, CompoundNBT nbt)
    {
        final PacketBuffer buf = new PacketBuffer(dataOut);
        try
        {
            buf.writeNbt(nbt);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void writePos(ByteBuf dataOut, BlockPos pos)
    {
        dataOut.writeInt(pos.getX());
        dataOut.writeInt(pos.getY());
        dataOut.writeInt(pos.getZ());
    }

    public static void writeString(ByteBuf dataOut, String str)
    {
        if (str == null)
        {
            dataOut.writeInt(-1);
            return;
        }
        final byte[] bytes = str.getBytes();
        dataOut.writeInt(bytes.length);
        if (bytes.length > 0) dataOut.writeBytes(bytes);
    }

    public static void writeStringUTF8(ByteBuf dataOut, String str)
    {
        if (str == null)
        {
            dataOut.writeInt(-1);
            return;
        }
        final byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        dataOut.writeInt(bytes.length);
        if (bytes.length > 0) dataOut.writeBytes(bytes);
    }
}