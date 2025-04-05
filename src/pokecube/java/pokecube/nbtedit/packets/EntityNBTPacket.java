package pokecube.nbtedit.packets;

import org.apache.logging.log4j.Level;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import pokecube.nbtedit.NBTEdit;
import thut.core.common.network.EntityUpdate;
import thut.core.common.network.Packet;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;

public class EntityNBTPacket extends NBTPacket
{
    public static final PacketAssembly<EntityNBTPacket> ASSEMBLER = PacketAssembly.registerAssembler(
            EntityNBTPacket.class, EntityNBTPacket::new, PacketHandler.INSTANCE);

    /** The id of the entity being edited. */
    protected int entityID;

    /** Required default constructor. */
    public EntityNBTPacket()
    {
        super();
    }

    public EntityNBTPacket(final int entityID, final CompoundTag tag)
    {
        super();
        tag.putInt("_nbtedit_id", entityID);
        this.tag = tag;
    }

    @Override
    protected void onCompleteClient(Player player)
    {
        this.entityID = this.getTag().getInt("_nbtedit_id");
        this.getTag().remove("_nbtedit_id");
        NBTEdit.proxy.openEditGUI(this.entityID, this.getTag());
    }

    @Override
    protected void onCompleteServer(final ServerPlayer player)
    {
        this.entityID = this.getTag().getInt("_nbtedit_id");
        this.getTag().remove("_nbtedit_id");
        final Entity entity = player.level.getEntity(this.entityID);
        if (entity != null && NBTEdit.proxy.checkPermission(player)) try
        {
            final GameType preGameType = player.gameMode.getGameModeForPlayer();
            entity.load(this.getTag());
            NBTEdit.log(Level.TRACE, player.getName().getString() + " edited a tag -- Entity ID #" + this.entityID);
            NBTEdit.logTag(this.getTag());
            if (entity == player)
            { // Update player info
              // This is fairly hacky. Consider swapping
              // to an event driven system, where classes
              // can register to
              // receive entity edit events and provide
              // feedback/send packets as necessary.

                player.inventoryMenu.sendAllDataToRemote();
                final GameType type = player.gameMode.getGameModeForPlayer();
                if (preGameType != type) player.setGameMode(type);
                player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData()
                        .getFoodLevel(), player.getFoodData().getSaturationLevel()));
                player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience,
                        player.experienceLevel));
                player.onUpdateAbilities();
            }
            EntityUpdate.sendEntityUpdate(entity);
            NBTEdit.proxy.sendMessage(player, "Your changes have been saved", ChatFormatting.WHITE);
        }
        catch (final Throwable t)
        {
            NBTEdit.proxy.sendMessage(player, "Save Failed - Invalid NBT format for Entity", ChatFormatting.RED);
            NBTEdit.log(Level.WARN, player.getName().getString() + " edited a tag and caused an exception");
            NBTEdit.logTag(this.getTag());
            NBTEdit.throwing("EntityNBTPacket", "Handler.onMessage", t);
        }
        else NBTEdit.proxy.sendMessage(player, "Save Failed - Entity does not exist", ChatFormatting.RED);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:nbtedit_entity_data"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
