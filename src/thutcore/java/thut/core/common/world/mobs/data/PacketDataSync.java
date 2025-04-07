package thut.core.common.world.mobs.data;

import com.google.common.collect.Lists;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thut.api.entity.EntityProvider;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.ThutCore;
import thut.core.common.network.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketDataSync extends Packet
{
    public static void sync(final Entity tracked, final DataSync data, final int entity_id, final boolean all)
    {
        boolean init = data.needInit();
        data.clearNeedInit();
        List<Data<?>> list = all || init ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || list.isEmpty()) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        packet.type = (byte) (init ? 1 : 0);
        ThutCore.packets.sendToTrackingAndSelf(packet, tracked);
    }

    public static void sync(final ServerPlayer syncTo, final DataSync data, final int entity_id, final boolean all)
    {
        boolean init = data.needInit();
        List<Data<?>> list = all || init ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || list.isEmpty()) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        packet.type = (byte) (init ? 1 : 0);
        ThutCore.packets.sendTo(packet, syncTo);
    }

    int id;
    byte type;

    List<Data<?>> data = Lists.newArrayList();

    public PacketDataSync()
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
            try
            {
                int uid = buf.readInt();
                String tag = "", name = "";
                if(type==1){
                    tag = buf.readUtf();
                    name = buf.readUtf();
                }
                Data<?> val = DataSync_Impl.makeData(name, uid);
                val.setTag(tag);
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
    public void handleClient(Player player)
    {
        final Level world = player.level();
        final Entity mob = EntityProvider.provider.getEntity(world, id);
        if (mob == null) return;
        final DataSync sync = SyncHandler.getData(mob);
        if (type == 0) sync.update(this.data);
        else sync.init(this.data);
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

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("thutcore:data_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
