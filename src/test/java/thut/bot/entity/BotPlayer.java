package thut.bot.entity;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.compress.utils.Lists;

import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.bot.entity.ai.IBotAI;
import thut.core.common.network.EntityUpdate;

public class BotPlayer extends ServerPlayer
{

    public static final String PERMBOTORDER = "thutbot.perm.orderbot";

    public static final Pattern startPattern = Pattern.compile("(start)(\\s)(\\w+:\\w+)");

    private IBotAI maker;

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile);
        this.connection = new BotPlayerNetHandler(world.getServer(), this);
    }

    @Override
    public void tick()
    {
        ChunkPos cpos = this.chunkPosition();
        ServerLevel level = this.getLevel();

        if (maker != null) this.maker.tick();
        else if (this.getPersistentData().contains("ai_task"))
        {
            String key = this.getPersistentData().getString("ai_task");
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                this.maker = factory.create(this);
                this.maker.setKey(key);
                this.maker.start(null);
            }
        }
        else
        {
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            if (this.tickCount % 20 == 0) EntityUpdate.sendEntityUpdate(this);
        }

        if (cpos != this.chunkPosition())
        {
            level.getChunkSource().move(this);
        }
    }

    public void onChat(ServerChatEvent event)
    {
        ServerPlayer talker = event.getPlayer();
        if (talker instanceof BotPlayer) return;

        boolean isOrder = event.getMessage().contains(this.getName().getString());

        // Decide if we want to say something back?
        if (!isOrder) return;

        PermissionAPI.registerNode(PERMBOTORDER, DefaultPermissionLevel.OP, "Allowed to give orders to thutbots");
        String s1 = "I Am A Bot";
        chat(s1);

        if (!PermissionAPI.hasPermission(talker, PERMBOTORDER)) return;

        Matcher startOrder = startPattern.matcher(event.getMessage());

        if (startOrder.find())
        {
            String key = startOrder.group(3);
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                s1 = "Starting " + key;
                this.getPersistentData().putString("ai_task", key);
                if (this.maker != null) this.maker.end(talker);
                this.maker = factory.create(this);
                maker.setKey(key);
                if (!maker.init(event.getMessage()))
                {
                    chat("Invalid argument!");
                    this.getPersistentData().remove("ai_task");
                    this.maker = null;
                    return;
                }
                this.maker.start(talker);
                chat(s1);
            }
            else
            {
                s1 = "I don't know how to do that!";
                if (this.maker != null)
                {
                    this.maker.end(talker);
                    this.getPersistentData().remove("ai_task");
                    this.maker = null;
                }
                chat(s1);
                s1 = "What I know how to do:";
                chat(s1);
                for (String s : IBotAI.REGISTRY.keySet())
                {
                    chat(s);
                }
            }
        }
        else if (event.getMessage().contains("reset"))
        {
            if (this.maker != null) this.maker.end(talker);
            List<String> tags = Lists.newArrayList();
            tags.addAll(this.getPersistentData().getAllKeys());
            tags.forEach(s -> getPersistentData().remove(s));
            this.maker = null;
        }
    }

    public void chat(String message)
    {
        Component component = message.isEmpty() ? null
                : new TranslatableComponent("chat.type.text", this.getDisplayName(), message);
        Component component1 = new TranslatableComponent("chat.type.text", this.getDisplayName(), message);
        Component finalComponent = component1;
        this.server.getPlayerList().broadcastMessage(component1, (player) -> {
            return this.shouldFilterMessageTo(player) ? component : finalComponent;
        }, ChatType.CHAT, this.getUUID());
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
