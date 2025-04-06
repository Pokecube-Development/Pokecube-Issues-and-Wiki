package thut.core.common.world.mobs.data;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.api.entity.EntityProvider;
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
        if (list == null || list.isEmpty()) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        ThutCore.packets.sendToTrackingAndSelf(packet, tracked);
    }

    public static void sync(final ServerPlayer syncTo, final DataSync data, final int entity_id, final boolean all)
    {
        final List<Data<?>> list = all ? data.getAll() : data.getDirty();
        // Nothing to sync.
        if (list == null || list.isEmpty()) return;
        final PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        ThutCore.packets.sendTo(packet, syncTo);
    }

    public int id;

    public List<Data<?>> data = Lists.newArrayList();

    public PacketDataSync()
    {
    }

    public void read(final FriendlyByteBuf buf)
    {
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
    public void handleClient(Player player)
    {
        final Level world = player.level();
        final Entity mob = EntityProvider.provider.getEntity(world, id);
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

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:data_sync"));
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
