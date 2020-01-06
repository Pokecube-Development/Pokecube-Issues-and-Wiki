package thut.core.common.network;

import com.google.common.base.Function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler
{
    private final SimpleChannel INSTANCE;

    private int ID = 0;

    public PacketHandler(final ResourceLocation channel, final String version)
    {
        this.INSTANCE = NetworkRegistry.newSimpleChannel(channel, () -> version, s -> version.isEmpty() ? true
                : s.compareTo(version) >= 0, s -> version.isEmpty() ? true : s.compareTo(version) >= 0);
    }

    public SimpleChannel channel()
    {
        return this.INSTANCE;
    }

    private int nextID()
    {
        return this.ID++;
    }

    public <MSG extends Packet> void registerMessage(Class<MSG> clazz, Function<PacketBuffer, MSG> decoder)
    {
        this.INSTANCE.registerMessage(this.nextID(), clazz, Packet::write, decoder, Packet::handle);
    }

    public void sendTo(Packet message, ServerPlayerEntity target)
    {
        this.channel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    public void sendToServer(Packet message)
    {
        this.channel().sendToServer(message);
    }

    public void sendToTracking(Packet message, Entity tracked)
    {
        this.channel().send(PacketDistributor.TRACKING_ENTITY.with(() -> tracked), message);
    }

    public void sendToTracking(Packet message, IChunk tracked)
    {
        if (tracked instanceof Chunk) this.channel().send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) tracked),
                message);
    }

}
