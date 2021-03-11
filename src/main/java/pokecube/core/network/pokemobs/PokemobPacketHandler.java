package pokecube.core.network.pokemobs;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

/**
 * This class handles the packets sent for the IPokemob Entities.
 *
 * @author Thutmose
 */
public class PokemobPacketHandler
{
    public static class MessageServer extends Packet
    {

        public static final byte RETURN       = 0;
        public static final byte CANCELEVOLVE = 12;

        PacketBuffer buffer;;

        public MessageServer()
        {
        }

        public MessageServer(final byte messageid, final int entityId)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            this.buffer.writeByte(messageid);
            this.buffer.writeInt(entityId);
        }

        public MessageServer(final byte channel, final int id, final CompoundNBT nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            this.buffer.writeByte(channel);
            this.buffer.writeInt(id);
            this.buffer.writeNbt(nbt);
        }

        public MessageServer(final byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public MessageServer(final PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public void handleServer(final ServerPlayerEntity player)
        {
            final byte channel = this.buffer.readByte();
            final int id = this.buffer.readInt();
            final ServerWorld world = player.getLevel();
            final Entity entity = PokecubeCore.getEntityProvider().getEntity(world, id, true);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob == null || !player.getUUID().equals(pokemob.getOwnerId())) return;
            if (channel == MessageServer.RETURN) pokemob.onRecall();
            else if (channel == MessageServer.CANCELEVOLVE) pokemob.cancelEvolve();
        }

        @Override
        public void write(final PacketBuffer buf)
        {
            if (this.buffer == null) this.buffer = new PacketBuffer(Unpooled.buffer());
            buf.writeBytes(this.buffer);
        }
    }

    public static MessageServer makeServerPacket(final byte channel, final byte[] data)
    {
        final byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
            packetData[i] = data[i - 1];
        return new MessageServer(packetData);
    }
}
