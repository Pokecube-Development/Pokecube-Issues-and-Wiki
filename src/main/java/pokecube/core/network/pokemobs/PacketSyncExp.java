package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketSyncExp extends Packet
{
    public static void sendUpdate(IPokemob pokemob)
    {
        if (!pokemob.getEntity().isEffectiveAi()) return;
        final PacketSyncExp packet = new PacketSyncExp();
        packet.entityId = pokemob.getEntity().getId();
        packet.exp = pokemob.getExp();
        PokecubeCore.packets.sendToTracking(packet, pokemob.getEntity());
    }

    int entityId;
    int exp;

    public PacketSyncExp()
    {
    }

    public PacketSyncExp(FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.exp = buf.readInt();
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final int id = this.entityId;
        final int exp = this.exp;
        final Entity e = PokecubeCore.getEntityProvider().getEntity(player.getCommandSenderWorld(), id, true);
        final IPokemob mob = CapabilityPokemob.getPokemobFor(e);
        if (mob != null) mob.getMoveStats().exp = exp;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeInt(this.exp);
    }

}
