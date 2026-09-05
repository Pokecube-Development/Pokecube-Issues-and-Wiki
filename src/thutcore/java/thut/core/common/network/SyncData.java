package thut.core.common.network;

import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thut.api.Tracker;
import thut.api.entity.EntityProvider;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.SyncHandler;

import java.util.ArrayList;
import java.util.List;

public class SyncData extends Packet
{
    public static void sync(Entity tracked, DataSync data, int entity_id, boolean all)
    {
        boolean init = data.needInit();
        data.clearNeedInit();
        List<Data<?>> list = all || init ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || list.isEmpty()) return;
        SyncData packet = new SyncData();
        // Mark entire list as not dirty
        list.forEach(d -> {
            d.setDirty(false);
            if (ThutCore.conf.debug)
            {
                Tracker.SERVER_COUNTERS.computeIfAbsent("sync_data:" + d.getTag() + ":" + d.getName(),
                        _key -> new Tracker.Counter(_key, 200)).increment();
            }
        });
        packet.data = list;
        packet.id = entity_id;
        packet.type = (byte) (all || init ? 1 : 0);

        ThutCore.packets.sendToTrackingAndSelf(packet, tracked);
    }

    public static void sync(ServerPlayer syncTo, DataSync data, int entity_id, boolean all)
    {
        boolean init = data.needInit();
        data.clearNeedInit();
        List<Data<?>> list = all || init ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || list.isEmpty()) return;
        SyncData packet = new SyncData();
        packet.data = list;
        packet.id = entity_id;
        packet.type = (byte) (all || init ? 1 : 0);
        ThutCore.packets.sendTo(packet, syncTo);
    }

    int id;
    byte type;

    List<Data<?>> data = Lists.newArrayList();

    public SyncData()
    {
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.id = buf.readInt();
        this.type = buf.readByte();
        short num = buf.readShort();
        this.data = new ArrayList<>();
        if (num > 0) for (int i = 0; i < num; i++)
        {
            int uid = buf.readInt();
            String tag = "", name = "";
            if (type == 1)
            {
                tag = buf.readUtf();
                name = buf.readUtf();
            }
            try
            {
                Data<?> val = DataSync_Impl.makeData(name, uid);
                val.setTag(tag);
                val.read(buf);
                this.data.add(val);
            }
            catch (final Exception e)
            {
                ThutCore.LOGGER.error("Error loading data for {}, {}, {}, {}, {}", id, uid, tag, name, e);
            }
        }
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.id);
        buf.writeByte(this.type);
        final short num = (short) this.data.size();
        buf.writeShort(num);
        for (int i = 0; i < num; i++)
        {
            final Data<?> val = this.data.get(i);
            buf.writeInt(val.getUID());
            if (this.type == 1)
            {
                buf.writeUtf(val.getTag());
                buf.writeUtf(val.getName());
            }
            val.write(buf);
        }
    }

    @Override
    public void handleClient(Player player)
    {
        final Level world = player.level();
        final Entity mob = EntityProvider.provider.getEntity(world, id);
        if (mob == null) return;
        final DataSync sync = SyncHandler.getData(mob);
        if (type == 0) sync.update(this.data);
        else sync.init(this.data);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("thutcore:data_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
