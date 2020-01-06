package pokecube.core.network.packets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import pokecube.core.PokecubeCore;
import pokecube.core.items.pokecubes.EntityPokecube;
import thut.core.common.network.Packet;

public class PacketPokecube extends Packet
{
    public static void sendMessage(PlayerEntity player, int id, long renderTime)
    {
        final PacketPokecube toSend = new PacketPokecube(id, renderTime);
        PokecubeCore.packets.sendTo(toSend, (ServerPlayerEntity) player);
    }

    int  id;
    long time;

    public PacketPokecube()
    {
    }

    public PacketPokecube(int id, long renderTime)
    {
        this.time = renderTime;
        this.id = id;
    }

    public PacketPokecube(PacketBuffer buf)
    {
        this.time = buf.readLong();
        this.id = buf.readInt();
    }

    @Override
    public void handleClient()
    {
        final Entity e = PokecubeCore.proxy.getWorld().getEntityByID(this.id);
        if (e instanceof EntityPokecube) ((EntityPokecube) e).reset = this.time;
    }

    @Override
    public void write(PacketBuffer buf)
    {
        buf.writeLong(this.time);
        buf.writeInt(this.id);
    }
}