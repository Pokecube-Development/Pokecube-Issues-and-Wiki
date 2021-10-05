package thut.wearables.network;

import com.google.common.base.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class PacketHandler
{
    private static boolean canClientConnect(final String versionClient, final String versionServer)
    {
        return true;
    }

    private static boolean canServerConnect(final String versionClient, final String versionServer)
    {
        return true;
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

    public <MSG extends Packet> void registerMessage(final Class<MSG> clazz, final Function<FriendlyByteBuf, MSG> decoder)
    {
        this.INSTANCE.registerMessage(this.nextID(), clazz, Packet::write, decoder, Packet::handle);
    }

    public void sendTo(final Packet message, final ServerPlayer target)
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

    public void sendToTracking(final Packet message, final ChunkAccess tracked)
    {
        if (tracked instanceof LevelChunk) this.channel().send(PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) tracked),
                message);
    }

}
