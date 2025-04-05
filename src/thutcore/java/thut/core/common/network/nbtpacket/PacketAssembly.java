package thut.core.common.network.nbtpacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import thut.core.common.ThutCore;
import thut.core.common.network.PacketHandler;

/**
 * Copied from BetterQuesting, then modified to meet my needs.
 */
public final class PacketAssembly<T extends NBTPacket>
{
    public static interface Factory<T>
    {
        T create();
    }

    public static <K extends NBTPacket> PacketAssembly<K> registerAssembler(final Class<K> clazz,
            final Factory<K> factory, final PacketHandler handler)
    {
        final PacketAssembly<K> assembler = new PacketAssembly<>(factory, handler);
        synchronized (ASSEMBLERS)
        {
            PacketAssembly.ASSEMBLERS.put(clazz, assembler);
        }
        return assembler;
    }

    public static final Map<Class<? extends NBTPacket>, PacketAssembly<?>> ASSEMBLERS = new HashMap<>();

    // Message assigned packet buffers
    private final HashMap<UUID, byte[]> buffer = new HashMap<>();

    // Internal server packet buffer (server to server or client side)
    private byte[] serverBuf = null;

    private static final int bufSize = 20480; // 20KB

    private final Factory<T> factory;

    private final PacketHandler handler;

    public PacketAssembly(final Factory<T> factory, final PacketHandler handler)
    {
        this.factory = factory;
        this.handler = handler;
    }

    public void sendTo(final CompoundTag packet, final ServerPlayer player)
    {
        sendTo(packet, p -> handler.sendTo(p, player));
    }

    public void sendToTracking(final CompoundTag packet, final LevelChunk chunk)
    {
        sendTo(packet, p -> handler.sendToTracking(p, chunk));
    }

    public void sendToTracking(final CompoundTag packet, final Entity entity)
    {
        sendTo(packet, p -> handler.sendToTracking(p, entity));
    }

    public void sendToServer(final CompoundTag packet)
    {
        sendTo(packet, p -> handler.sendToServer(p));
    }

    public void sendTo(final CompoundTag packet, Consumer<T> processor)
    {
        final UUID id = UUID.randomUUID();
        final List<CompoundTag> tags = this.splitPacket(id, packet);
        for (final CompoundTag tag : tags)
        {
            final T newPacket = this.factory.create();
            newPacket.setTag(tag);
            processor.accept(newPacket);
        }
    }

    protected CompoundTag onRead(final CompoundTag tag)
    {
        if (tag == null)
        {
            ThutCore.LOGGER.error("Error with bad packet! Tag:" + tag, new IllegalStateException());
            return null;
        }
        final UUID id = tag.getUUID("id");
        final CompoundTag made = this.assemblePacket(id, tag);
        return made;
    }

    private List<CompoundTag> splitPacket(final UUID id, final CompoundTag tags)
    {
        try
        {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tags, baos);
            baos.flush();
            final byte[] data = baos.toByteArray();
            baos.close();
            final int req = Mth.ceil(data.length / (float) PacketAssembly.bufSize);
            final List<CompoundTag> pkts = new ArrayList<>(req);

            for (int p = 0; p < req; p++)
            {
                final int idx = p * PacketAssembly.bufSize;
                final int s = Math.min(data.length - idx, PacketAssembly.bufSize);
                final CompoundTag container = new CompoundTag();
                final byte[] part = new byte[s];
                System.arraycopy(data, idx, part, 0, s);
                // If the buffer isn't yet created, how big is it
                container.putInt("size", data.length);
                // Where should this piece start writing too
                container.putInt("start", idx);
                container.putBoolean("end", p == req - 1);
                // The raw byte data to write
                container.put("data", new ByteArrayTag(part));
                // Include who's packet we go to
                container.putUUID("id", id);
                pkts.add(container);
            }
            return pkts;
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Unable to split build packet!", e);
            return Collections.emptyList();
        }
    }

    /**
     * Appends a packet onto the buffer and returns an assembled NBTTagCompound
     * when complete
     */
    private CompoundTag assemblePacket(final UUID id, final CompoundTag tags)
    {
        final int size = tags.getInt("size");
        final int index = tags.getInt("start");
        final boolean end = tags.getBoolean("end");
        byte[] data = tags.getByteArray("data");

        byte[] tmp = this.getBuffer(id);

        if (tmp == null)
        {
            tmp = new byte[size];
            this.setBuffer(id, tmp);
        }
        else if (tmp.length != size)
        {
            ThutCore.LOGGER.error("Unexpected change in ThutCore packet byte length: " + size + " > " + tmp.length);
            this.clearBuffer(id);
            return null;
        }

        System.arraycopy(data, 0, tmp, index, data.length);

        if (end)
        {
            this.clearBuffer(id);
            try
            {
                return NbtIo.readCompressed(new ByteArrayInputStream(tmp), NbtAccounter.create(104857600L));
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Size: {} {}, {}", size, tmp.length, this.factory.create().getClass());
                throw new RuntimeException("Unable to dis-assemble packet", e);
            }
        }

        return null;
    }

    private byte[] getBuffer(final UUID id)
    {
        if (id == null) return this.serverBuf;
        else synchronized (this.buffer)
        {
            return this.buffer.get(id);
        }
    }

    private void setBuffer(final UUID id, final byte[] value)
    {
        if (id == null) this.serverBuf = value;
        else synchronized (this.buffer)
        {
            if (this.buffer.containsKey(id)) throw new IllegalStateException(
                    "Attepted to start more than one ThutCore packet assembly for UUID " + id.toString());

            this.buffer.put(id, value);
        }
    }

    private void clearBuffer(final UUID id)
    {
        if (id == null) this.serverBuf = null;
        else synchronized (this.buffer)
        {
            this.buffer.remove(id);
        }
    }

}