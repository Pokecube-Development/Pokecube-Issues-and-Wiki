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
import thut.core.common.ThutCore;

public class PacketHandler
{
    private static boolean canClientConnect(final String versionClient, final String versionServer)
    {
        ThutCore.LOGGER.debug("Client-Server Test: {} -> {}", versionClient, versionServer);
        return versionClient.equals(versionServer);
    }

    private static boolean canServerConnect(final String versionClient, final String versionServer)
    {
        ThutCore.LOGGER.debug("Server-Client Test: {} -> {}", versionServer, versionClient);
        return versionClient.equals(versionServer);
    }

    private final SimpleChannel INSTANCE;

    private int ID = 0;

    public PacketHandler(final ResourceLocation channel, final String version)
    {
        this.INSTANCE = NetworkRegistry.newSimpleChannel(channel, () -> version, s -> PacketHandler.canClientConnect(
                version, s), s -> PacketHandler.canServerConnect(s, version));
    }

    public SimpleChannel channel()
    {
        return this.INSTANCE;
    }

    private int nextID()
    {
        return this.ID++;
    }

    public <MSG extends Packet> void registerMessage(final Class<MSG> clazz, final Function<PacketBuffer, MSG> decoder)
    {
        this.INSTANCE.registerMessage(this.nextID(), clazz, Packet::write, decoder, Packet::handle);
    }

    public void sendTo(final Packet message, final ServerPlayerEntity target)
    {
        this.channel().send(PacketDistributor.PLAYER.with(() -> target), message);
    }

    public void sendToServer(final Packet message)
    {
        this.channel().sendToServer(message);
    }

    public void sendToTracking(final Packet message, final Entity tracked)
    {
        this.channel().send(PacketDistributor.TRACKING_ENTITY.with(() -> tracked), message);
    }

    public void sendToTracking(final Packet message, final IChunk tracked)
    {
        if (tracked instanceof Chunk) this.channel().send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) tracked),
                message);
    }

}
