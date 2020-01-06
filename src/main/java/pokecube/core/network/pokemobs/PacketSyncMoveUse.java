package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketSyncMoveUse extends Packet
{
    public static void sendUpdate(IPokemob pokemob)
    {
        if (pokemob.getEntity().getEntityWorld().isRemote || !(pokemob.getOwner() instanceof PlayerEntity))
            return;
        final PacketSyncMoveUse packet = new PacketSyncMoveUse();
        packet.entityId = pokemob.getEntity().getEntityId();
        packet.index = pokemob.getMoveIndex();
        PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) pokemob.getOwner());
    }

    int entityId;
    int index;

    public PacketSyncMoveUse()
    {
    }

    public PacketSyncMoveUse(PacketBuffer buf)
    {
        this.entityId = buf.readInt();
        this.index = buf.readInt();
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final int index = this.index;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null) mob.getMoveStats().lastMove = mob.getMove(index);
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeInt(this.entityId);
        buf.writeInt(this.index);
    }

}
