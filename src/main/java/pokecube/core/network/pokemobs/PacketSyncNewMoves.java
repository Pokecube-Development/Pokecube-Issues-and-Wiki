package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketSyncNewMoves extends Packet
{
    public static void sendUpdatePacket(final IPokemob pokemob)
    {
        if (pokemob.getOwner() instanceof ServerPlayer)
        {
            final ServerPlayer player = (ServerPlayer) pokemob.getOwner();
            final ListTag newMoves = new ListTag();
            for (final String s : pokemob.getMoveStats().newMoves) newMoves.add(StringTag.valueOf(s));
            final PacketSyncNewMoves packet = new PacketSyncNewMoves();
            packet.data.put(TagNames.NEWMOVES, newMoves);
            packet.data.putInt("i", pokemob.getMoveStats().num);
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
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null)
        {
            final ListTag newMoves = (ListTag) data.get(TagNames.NEWMOVES);
            pokemob.getMoveStats().newMoves.clear();
            pokemob.setLeaningMoveIndex(data.getInt("i"));
            for (int i = 0; i < newMoves.size(); i++)
                pokemob.getMoveStats().addPendingMove(newMoves.getString(i), null);
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
