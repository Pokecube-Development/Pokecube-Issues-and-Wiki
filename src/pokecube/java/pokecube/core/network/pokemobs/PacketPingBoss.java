package pokecube.core.network.pokemobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.core.common.network.Packet;

public class PacketPingBoss extends Packet
{
    public static void onNewBossEvent(final IPokemob pokemob)
    {
        final PacketPingBoss packet = new PacketPingBoss();
        packet.entityId = pokemob.getEntity().getId();
        PokecubeCore.packets.sendToTracking(packet, pokemob.getEntity());
    }

    public int entityId;

    public PacketPingBoss()
    {
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
    }

    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null && pokemob.getBossInfo() != null) pokemob.getBossInfo().addPlayer(player);
    }

    @Override
    public void handleClient(Player player)
    {
        final PacketPingBoss packet = new PacketPingBoss();
        packet.entityId = this.entityId;
        PokecubeCore.packets.sendToServer(packet);
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:ping_boss"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
