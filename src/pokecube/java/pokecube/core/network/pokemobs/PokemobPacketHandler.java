package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
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

        public static final byte RETURN = 0;
        public static final byte CANCELEVOLVE = 12;

        byte message;
        int entityId;

        public MessageServer()
        {
        }

        public MessageServer(final byte messageid, final int entityId)
        {
            this.message = messageid;
            this.entityId = entityId;
        }

        public void read(final FriendlyByteBuf buffer)
        {
            this.message = buffer.readByte();
            this.entityId = buffer.readInt();
        }

        @Override
        public void handleServer(final ServerPlayer player)
        {
            final byte channel = this.message;
            final int id = this.entityId;
            final ServerLevel world = (ServerLevel) player.level();
            final Entity entity = PokecubeAPI.getEntityProvider().getEntity(world, id, true);
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            if (pokemob == null || !player.getUUID().equals(pokemob.getOwnerId())) return;
            if (channel == MessageServer.RETURN) pokemob.onRecall();
            else if (channel == MessageServer.CANCELEVOLVE) pokemob.cancelEvolve();
        }

        @Override
        public void write(final FriendlyByteBuf buf)
        {
            buf.writeByte(message);
            buf.writeInt(entityId);
        }

        private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:general_to_server"));

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }
    }
}
