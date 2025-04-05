package pokecube.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokecubes.EntityPokecube;
import thut.core.common.network.Packet;

public class PacketPokecube extends Packet
{
    public static void sendMessage(Player player, int id, long renderTime)
    {
        final PacketPokecube toSend = new PacketPokecube(id, renderTime);
        PokecubeCore.packets.sendTo(toSend, (ServerPlayer) player);
    }

    int id;
    long time;

    public PacketPokecube()
    {}

    public PacketPokecube(int id, long renderTime)
    {
        this.time = renderTime;
        this.id = id;
    }

    public void read(FriendlyByteBuf buf)
    {
        this.time = buf.readLong();
        this.id = buf.readInt();
    }

    @Override
    public void handleClient(Player player)
    {
        final Entity e = player.level().getEntity(this.id);
        if (e instanceof EntityPokecube cube) cube.reset = this.time;
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeLong(this.time);
        buf.writeInt(this.id);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:sync_pokecube_time"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}