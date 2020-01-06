package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TagNames;
import thut.core.common.network.Packet;

public class PacketSyncNewMoves extends Packet
{
    public static void sendUpdatePacket(IPokemob pokemob)
    {
        if (pokemob.getOwner() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) pokemob.getOwner();
            final ListNBT newMoves = new ListNBT();
            for (final String s : pokemob.getMoveStats().newMoves)
                newMoves.add(new StringNBT(s));
            final PacketSyncNewMoves packet = new PacketSyncNewMoves();
            packet.data.put(TagNames.NEWMOVES, newMoves);
            packet.entityId = pokemob.getEntity().getEntityId();
            PokecubeCore.packets.sendTo(packet, player);
        }
    }

    public int entityId;

    public CompoundNBT data = new CompoundNBT();

    public PacketSyncNewMoves()
    {
        super(null);
    }

    public PacketSyncNewMoves(PacketBuffer buffer)
    {
        super(buffer);
        this.entityId = buffer.readInt();
        this.data = buffer.readCompoundTag();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final CompoundNBT data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null)
        {
            final ListNBT newMoves = (ListNBT) data.get(TagNames.NEWMOVES);
            pokemob.getMoveStats().newMoves.clear();
            for (int i = 0; i < newMoves.size(); i++)
                if (!pokemob.getMoveStats().newMoves.contains(newMoves.getString(i))) pokemob.getMoveStats().newMoves
                        .add(newMoves.getString(i));
        }
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeCompoundTag(this.data);
    }
}
