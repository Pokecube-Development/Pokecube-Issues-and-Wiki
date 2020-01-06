package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import pokecube.nbtedit.NBTEdit;
import pokecube.nbtedit.NBTHelper;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.Packet;

public class EntityNBTPacket extends Packet
{
    /** The id of the entity being edited. */
    protected int         entityID;
    /** The nbt data of the entity. */
    protected CompoundNBT tag;

    /** Required default constructor. */
    public EntityNBTPacket()
    {
    }

    public EntityNBTPacket(int entityID, CompoundNBT tag)
    {
        this.entityID = entityID;
        this.tag = tag;
    }

    public EntityNBTPacket(PacketBuffer buf)
    {
        this.entityID = buf.readInt();
        this.tag = NBTHelper.readNbtFromBuffer(buf);
    }

    @Override
    public void handleClient()
    {
        NBTEdit.proxy.openEditGUI(this.entityID, this.tag);
    }

    @Override
    public void handleServer(ServerPlayerEntity player)
    {
        final Entity entity = player.world.getEntityByID(this.entityID);
        if (entity != null && NBTEdit.proxy.checkPermission(player)) try
        {
            final GameType preGameType = player.interactionManager.getGameType();
            entity.read(this.tag);
            NBTEdit.log(Level.TRACE, player.getName() + " edited a tag -- Entity ID #" + this.entityID);
            NBTEdit.logTag(this.tag);
            if (entity == player)
            { // Update player info
              // This is fairly hacky. Consider swapping
              // to an event driven system, where classes
              // can register to
              // receive entity edit events and provide
              // feedback/send packets as necessary.

                player.sendContainerToPlayer(player.container);
                final GameType type = player.interactionManager.getGameType();
                if (preGameType != type) player.setGameType(type);
                player.connection.sendPacket(new SUpdateHealthPacket(player.getHealth(), player.getFoodStats()
                        .getFoodLevel(), player.getFoodStats().getSaturationLevel()));
                player.connection.sendPacket(new SSetExperiencePacket(player.experience, player.experienceTotal,
                        player.experienceLevel));
                player.sendPlayerAbilities();
            }
            EntityUpdate.sendEntityUpdate(entity);
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
    }

}
