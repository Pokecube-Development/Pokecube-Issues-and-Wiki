package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TagNames;
import thut.core.common.network.Packet;

public class PacketSyncNewMoves extends Packet
{
    public static void sendUpdatePacket(final IPokemob pokemob)
    {
        if (pokemob.getOwner() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) pokemob.getOwner();
            final ListTag newMoves = new ListTag();
            for (final String s : pokemob.getMoveStats().newMoves)
                newMoves.add(StringTag.valueOf(s));
            final PacketSyncNewMoves packet = new PacketSyncNewMoves();
            packet.data.put(TagNames.NEWMOVES, newMoves);
            packet.entityId = pokemob.getEntity().getId();
            PokecubeCore.packets.sendTo(packet, player);
        }
    }

    public int entityId;

    public CompoundTag data = new CompoundTag();

    public PacketSyncNewMoves()
    {
        super(null);
    }

    public PacketSyncNewMoves(final FriendlyByteBuf buffer)
    {
        super(buffer);
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(e);
        if (pokemob != null)
        {
            final ListTag newMoves = (ListTag) data.get(TagNames.NEWMOVES);
            pokemob.getMoveStats().newMoves.clear();
            for (int i = 0; i < newMoves.size(); i++)
                if (!pokemob.getMoveStats().newMoves.contains(newMoves.getString(i))) pokemob.getMoveStats().newMoves
                .add(newMoves.getString(i));
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
