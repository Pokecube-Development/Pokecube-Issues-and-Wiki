package pokecube.nbtedit;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class NBTHelper
{

    public static Map<String, Tag> getMap(CompoundTag tag)
    {
        return tag.tags;
    }

    public static Tag getTagAt(ListTag tag, int index)
    {
        final List<Tag> list = tag.list;
        return list.get(index);
    }

    public static CompoundTag nbtRead(DataInputStream in) throws IOException
    {
        return NbtIo.read(in);
    }

    public static void nbtWrite(CompoundTag compound, DataOutput out) throws IOException
    {
        NbtIo.write(compound, out);
    }

    public static CompoundTag readNbtFromBuffer(ByteBuf buf)
    {
        final int index = buf.readerIndex();
        final byte isNull = buf.readByte();

        if (isNull == 0) return null;
        // restore index after checking to make sure the tag wasn't null/
        buf.readerIndex(index);
        try
        {
            return NbtIo.read(new ByteBufInputStream(buf), new NbtAccounter(2097152L));
        }
        catch (final IOException ioexception)
        {
            throw new EncoderException(ioexception);
        }
    }

    public static void writeToBuffer(CompoundTag nbt, ByteBuf buf)
    {
        if (nbt == null) buf.writeByte(0);
        else try
        {
            NbtIo.write(nbt, new ByteBufOutputStream(buf));
        }
        catch (final IOException e)
        {
            throw new EncoderException(e);
        }
    }
}
