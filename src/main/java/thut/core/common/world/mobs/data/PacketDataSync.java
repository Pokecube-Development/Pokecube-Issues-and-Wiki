package thut.core.common.world.mobs.data;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.network.Packet;

public class PacketDataSync extends Packet
{
    public static void sync(final Entity tracked, final DataSync data, final int entity_id, final boolean all)
    {
        final List<Data<?>> list = all ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || tracked == null) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        ThutCore.packets.sendToTracking(packet, tracked);
        if (tracked instanceof ServerPlayer) ThutCore.packets.sendTo(packet, (ServerPlayer) tracked);
    }

    public static void sync(final ServerPlayer syncTo, final DataSync data, final int entity_id, final boolean all)
    {
        final List<Data<?>> list = all ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        ThutCore.packets.sendTo(packet, syncTo);
    }

    public int id;

    public List<Data<?>> data = Lists.newArrayList();

    public PacketDataSync()
    {
        super(null);
    }

    public PacketDataSync(final FriendlyByteBuf buf)
    {
        super(buf);
        this.id = buf.readInt();
        final byte num = buf.readByte();
        if (num > 0) for (int i = 0; i < num; i++)
        {
            final int uid = buf.readInt();
            try
            {
                final Data<?> val = DataSync_Impl.makeData(uid);
                val.read(buf);
                this.data.add(val);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void handleClient()
    {
        final Level world = net.minecraft.client.Minecraft.getInstance().level;
        final Entity mob = world.getEntity(this.id);
        if (mob == null) return;
        final DataSync sync = SyncHandler.getData(mob);
        if (sync == null) return;
        sync.update(this.data);
        return;
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.id);
        final byte num = (byte) this.data.size();
        buf.writeByte(num);
        for (int i = 0; i < num; i++)
        {
            final Data<?> val = this.data.get(i);
            buf.writeInt(val.getUID());
            val.write(buf);
        }
    }
}
