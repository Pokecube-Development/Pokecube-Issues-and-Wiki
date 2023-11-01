package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.core.common.network.Packet;

public class PacketChangeForme extends Packet
{
    public static void sendPacketToTracking(final Entity mob, final PokedexEntry forme)
    {
        if (mob.level.isClientSide()) return;
        final PacketChangeForme packet = new PacketChangeForme();
        packet.entityId = mob.getId();
        packet.forme = forme;
        PokecubeCore.packets.sendToTracking(packet, mob);
    }

    int entityId;

    PokedexEntry forme;

    public PacketChangeForme()
    {}

    public PacketChangeForme(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.forme = Database.getEntry(buffer.readUtf(20));
    }

    @Override
    public void handleClient()
    {
        final Player player = PokecubeCore.proxy.getPlayer();
        final Entity mob = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), this.entityId, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob == null) return;
        pokemob.setPokedexEntry(this.forme);
    }

    @Override
    public void write(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        buffer.writeInt(this.entityId);
        if (this.forme != null) buffer.writeUtf(this.forme.getName());
        else buffer.writeUtf("");
    }

}
