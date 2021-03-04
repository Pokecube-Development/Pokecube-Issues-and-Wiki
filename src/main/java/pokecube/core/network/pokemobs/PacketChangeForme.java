package pokecube.core.network.pokemobs;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.common.network.Packet;

public class PacketChangeForme extends Packet
{
    public static void sendPacketToTracking(final Entity mob, final PokedexEntry forme)
    {
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getEntityId();
        packet.forme = forme;
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    int entityId;

    PokedexEntry forme;

    public PacketChangeForme()
    {
    }

    public PacketChangeForme(final PacketBuffer buffer)
    {
        this.entityId = buffer.readInt();
        this.forme = Database.getEntry(buffer.readString(20));
    }

    @Override
    public void handleClient()
    {
        final PlayerEntity player = PokecubeCore.proxy.getPlayer();
        final Entity mob = PokecubeCore.getEntityProvider().getEntity(player.getEntityWorld(), this.entityId, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setPokedexEntry(this.forme);
    }

    @Override
    public void write(final PacketBuffer buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(this.entityId);
        if (this.forme != null) buffer.writeString(this.forme.getName());
        else buffer.writeString("");
    }

}
