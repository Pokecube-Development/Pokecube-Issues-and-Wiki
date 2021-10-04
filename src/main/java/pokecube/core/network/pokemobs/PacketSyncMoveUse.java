package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketSyncMoveUse extends Packet
{
    public static void sendUpdate(IPokemob pokemob)
    {
        if (pokemob.getEntity().getCommandSenderWorld().isClientSide || !(pokemob.getOwner() instanceof Player))
            return;
        final PacketSyncMoveUse packet = new PacketSyncMoveUse();
        packet.entityId = pokemob.getEntity().getId();
        packet.index = pokemob.getMoveIndex();
        PokecubeCore.packets.sendTo(packet, (ServerPlayer) pokemob.getOwner());
    }

    int entityId;
    int index;

    public PacketSyncMoveUse()
    {
    }

    public PacketSyncMoveUse(FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.index = buf.readInt();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final int index = this.index;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null) mob.getMoveStats().lastMove = mob.getMove(index);
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeInt(this.index);
    }

}
