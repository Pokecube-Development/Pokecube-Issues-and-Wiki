package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.NBTHelper;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.network.Packet;

public class CustomNBTPacket extends Packet
{
    /** The id of the entity being edited. */
    protected int         entityID;
    /** The nbt data of the entity. */
    protected CompoundNBT tag;
    /** The custom name tag */
    protected String      customName;

    /** Required default constructor. */
    public CustomNBTPacket()
    {
    }

    public CustomNBTPacket(int entityID, String customName, CompoundNBT tag)
    {
        this.entityID = entityID;
        this.customName = customName;
        this.tag = tag;
    }

    public CustomNBTPacket(PacketBuffer buf)
    {
        this.entityID = buf.readInt();
        this.tag = NBTHelper.readNbtFromBuffer(buf);
        this.customName = new PacketBuffer(buf).readString(30);
    }

    @Override
    public void handleClient()
    {
        NBTEdit.proxy.openEditGUI(this.entityID, this.customName, this.tag);
    }

    @Override
    public void handleServer(ServerPlayerEntity player)
    {
        final Entity entity = player.world.getEntityByID(this.entityID);
        if (entity != null && NBTEdit.proxy.checkPermission(player)) try
        {
            final CompoundNBT tag = this.tag;
            final PlayerData data = PlayerDataHandler.getInstance().getPlayerData(entity.getCachedUniqueIdString())
                    .getData(this.customName);
            data.readFromNBT(tag);
            NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Entity ID #" + this.entityID);
            NBTEdit.logTag(this.tag);
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", TextFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.tag);
            NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
        }
        else NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", TextFormatting.RED);
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeInt(this.entityID);
        NBTHelper.writeToBuffer(this.tag, buf);
        new PacketBuffer(buf).writeString(this.customName);
    }
}
