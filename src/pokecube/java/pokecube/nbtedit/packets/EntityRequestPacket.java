package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.Packet;

public class EntityRequestPacket extends Packet
{
    /** The id of the entity being requested. */
    private int entityID;

    /** Required default constructor. */
    public EntityRequestPacket()
    {
    }

    public EntityRequestPacket(final int entityID)
    {
        this.entityID = entityID;
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.entityID = buf.readInt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        NBTEdit.log(Level.TRACE, player.getName().getString() + " requested entity with Id #" + this.entityID);
        PacketHandler.sendEntity(player, this.entityID);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityID);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:nbtedit_entity_request"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
