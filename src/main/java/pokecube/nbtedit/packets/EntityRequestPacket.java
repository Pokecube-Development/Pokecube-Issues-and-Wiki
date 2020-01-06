package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
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

    public EntityRequestPacket(int entityID)
    {
        this.entityID = entityID;
    }

    public EntityRequestPacket(PacketBuffer buf)
    {
        this.entityID = buf.readInt();
    }

    @Override
    public void handleServer(ServerPlayerEntity player)
    {
        NBTEdit.log(Level.TRACE, player.getName() + " requested entity with Id #" + this.entityID);
        PacketHandler.sendEntity(player, this.entityID);
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeInt(this.entityID);
    }

}
