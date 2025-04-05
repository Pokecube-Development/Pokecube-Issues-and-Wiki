package thut.wearables.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class PacketHandler
{
    private String version;

    private List<Class<Packet>> TO_SERVER = new ArrayList<>();
    private List<Class<Packet>> TO_CLIENT = new ArrayList<>();
    private List<Class<Packet>> TO_BOTHCS = new ArrayList<>();

    public PacketHandler(final String version)
    {
        this.version = version;
        final IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        modEventBus.addListener(this::onPayloadRegister);
    }

    private void onPayloadRegister(RegisterPayloadHandlersEvent event)
    {
        var reg = event.registrar(version);

        TO_SERVER.forEach((packet) -> {
            try
            {
                var inst = packet.getConstructor().newInstance();
                @SuppressWarnings("unchecked")
                CustomPacketPayload.Type<Packet> type = (Type<Packet>) inst.type();
                reg.commonToServer(type, inst, inst);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        TO_CLIENT.forEach((packet) -> {
            try
            {
                var inst = packet.getConstructor().newInstance();
                @SuppressWarnings("unchecked")
                CustomPacketPayload.Type<Packet> type = (Type<Packet>) inst.type();
                reg.commonToClient(type, inst, inst);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        TO_BOTHCS.forEach((packet) -> {
            try
            {
                var inst = packet.getConstructor().newInstance();
                @SuppressWarnings("unchecked")
                CustomPacketPayload.Type<Packet> type = (Type<Packet>) inst.type();
                reg.commonBidirectional(type, inst, inst);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <MSG extends Packet> void registerToClientMessage(Class<MSG> clazz)
    {
        TO_CLIENT.add((Class<Packet>) clazz);
    }

    @SuppressWarnings("unchecked")
    public <MSG extends Packet> void registerToServerMessage(Class<MSG> clazz)
    {
        TO_SERVER.add((Class<Packet>) clazz);
    }

    @SuppressWarnings("unchecked")
    public <MSG extends Packet> void registerBiDirectionalMessage(Class<MSG> clazz)
    {
        TO_BOTHCS.add((Class<Packet>) clazz);
    }

    public void sendTo(final Packet message, final ServerPlayer target)
    {
        PacketDistributor.sendToPlayer(target, message);
    }

    public void sendToServer(final Packet message)
    {
        PacketDistributor.sendToServer(message);
    }

    public void sendToTracking(final Packet message, final Entity tracked)
    {
        PacketDistributor.sendToPlayersTrackingEntity(tracked, message);
    }

    public void sendToTrackingAndSelf(final Packet message, final Entity tracked)
    {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(tracked, message);
    }

    public void sendToTracking(final Packet message, final ChunkAccess tracked)
    {
        if (tracked instanceof LevelChunk)
        {
            var chunk = tracked;
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) chunk.getLevel(), chunk.getPos(), message);
        }
    }
}