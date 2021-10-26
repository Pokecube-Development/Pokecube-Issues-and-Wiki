package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.network.NBTPacket;
import thut.core.common.network.PacketAssembly;

public class CustomNBTPacket extends NBTPacket
{

    public static final PacketAssembly<CustomNBTPacket> ASSEMBLER = PacketAssembly.registerAssembler(
            CustomNBTPacket.class, CustomNBTPacket::new, PacketHandler.INSTANCE);

    /** Required default constructor. */
    public CustomNBTPacket()
    {
        super();
    }

    public CustomNBTPacket(final PacketBuffer buffer)
    {
        super(buffer);
    }

    public CustomNBTPacket(final int entityID, final String customName, final CompoundNBT tag)
    {
        super();
        tag.putInt("_nbtedit_id", entityID);
        tag.putString("_nbtedit_name", customName);
        this.tag = tag;
    }

    @Override
    protected void onCompleteClient()
    {
        final int entityID = this.getTag().getInt("_nbtedit_id");
        final String customName = this.getTag().getString("_nbtedit_name");
        NBTEdit.proxy.openEditGUI(entityID, customName, this.getTag());
    }

    @Override
    protected void onCompleteServer(final ServerPlayerEntity player)
    {
        final int entityID = this.getTag().getInt("_nbtedit_id");
        final String customName = this.getTag().getString("_nbtedit_name");
        final Entity entity = player.level.getEntity(entityID);
        if (entity != null && NBTEdit.proxy.checkPermission(player)) try
        {
            final CompoundNBT tag = this.getTag();
            final PlayerData data = PlayerDataHandler.getInstance().getPlayerData(entity.getStringUUID())
                    .getData(customName);
            data.readFromNBT(tag);
            NBTEdit.log(Level.TRACE, player.getName().getString() + " edited a tag -- Entity ID #" + entityID);
            NBTEdit.logTag(this.getTag());
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", TextFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", TextFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName().getString() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.getTag());
            NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
        }
        else NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", TextFormatting.RED);
    }

}
