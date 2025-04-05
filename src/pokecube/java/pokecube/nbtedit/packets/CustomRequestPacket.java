package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.Packet;

public class CustomRequestPacket extends Packet
{
    /** The id of the entity being requested. */
    private int    entityID;
    /** the custom data type being requested */
    private String customName;

    /** Required default constructor. */
    public CustomRequestPacket()
    {
    }

    public CustomRequestPacket(final int entityID, final String customName)
    {
        this.entityID = entityID;
        this.customName = customName;
    }

    public void read(final FriendlyByteBuf buf)
    {
        this.entityID = buf.readInt();
        this.customName = new FriendlyByteBuf(buf).readUtf(30);
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        NBTEdit.log(Level.TRACE, player.getName().getString() + " requested entity with Id #" + this.entityID);
        PacketHandler.sendCustomTag(player, this.entityID, this.customName);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityID);
        new FriendlyByteBuf(buf).writeUtf(this.customName);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:nbtedit_custom_request"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
