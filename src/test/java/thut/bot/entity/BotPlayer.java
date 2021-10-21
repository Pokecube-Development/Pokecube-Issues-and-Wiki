package thut.bot.entity;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import thut.bot.entity.ai.IBotAI;

public class BotPlayer extends ServerPlayer
{
    private final IBotAI maker;

    public BotPlayer(final ServerLevel world, final GameProfile profile, final IBotAI.Factory ai)
    {
        super(world.getServer(), world, profile);
        this.connection = new BotPlayerNetHandler(world.getServer(), this);
        this.maker = ai.create(this);
    }

    @Override
    public void tick()
    {
        this.maker.tick();
    }

    private static class BotPlayerNetHandler extends ServerGamePacketListenerImpl
    {
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        public BotPlayerNetHandler(final MinecraftServer server, final ServerPlayer player)
        {
            super(server, BotPlayerNetHandler.DUMMY_CONNECTION, player);
        }

    //@formatter:off See FakePlayer for more things to overrige here if needed
        @Override public void disconnect(final Component message) { }
        @Override public void send(final Packet<?> packet) { }
        @Override public void send(final Packet<?> packet, @Nullable final GenericFutureListener<? extends Future<? super Void>> listener) { }
    //@formatter:on
    }
}
