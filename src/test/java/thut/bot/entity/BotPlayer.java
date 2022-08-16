package thut.bot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.ServerChatEvent;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.bot.ThutBot;
import thut.bot.ThutBot.BotEntry;
import thut.bot.entity.ai.IBotAI;
import thut.core.common.network.EntityUpdate;
import thut.lib.TComponent;

public class BotPlayer extends ServerPlayer implements Npc
{

    public static final String PERMBOTORDER = "thutbot.perm.orderbot";

    public static final Pattern STARTORDER = Pattern.compile("(start)(\\s)(\\w+:\\w+)");

    private IBotAI routine;

    private final BotEntry entry;

    public BotPlayer(final ServerLevel world, final GameProfile profile)
    {
        super(world.getServer(), world, profile);
        this.connection = new BotPlayerNetHandler(world.getServer(), this);
        entry = ThutBot.BOT_MAP.get(profile.getId());
        try
        {
            if (entry.getFile().exists()) this.getPersistentData().merge(NbtIo.read(entry.getFile()));
        }
        catch (Exception e)
        {
            ThutBot.LOGGER.error("Error loading saved tag for {}", entry.name);
            ThutBot.LOGGER.error(e);
        }

        if (this.getPersistentData().contains("_last_pos_"))
        {
            BlockPos pos = NbtUtils.readBlockPos(this.getPersistentData().getCompound("_last_pos_"));
            this.setPos(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void tick()
    {
        ChunkPos cpos = this.chunkPosition();
        ServerLevel level = this.getLevel();

        if (routine != null)
        {
            this.routine.tick();
            if (routine.isCompleted())
            {
                routine.end(null);
                this.getPersistentData().remove("ai_task");
                routine = null;
            }
        }
        else if (this.getPersistentData().contains("ai_task"))
        {
            String key = this.getPersistentData().getString("ai_task");
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                this.routine = factory.create(this);
                this.routine.setKey(key);
                this.routine.start(null);
            }
        }
        else
        {
            this.setHealth(this.getMaxHealth());
            this.dead = false;
            if (this.tickCount % 20 == 0) EntityUpdate.sendEntityUpdate(this);

            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);

            if (this.isInWater()) this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05, 0));
            else this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));

            this.move(MoverType.SELF, this.getDeltaMovement());
        }

        if (cpos != this.chunkPosition())
        {
            level.getChunkSource().move(this);
            this.getPersistentData().put("_last_pos_", NbtUtils.writeBlockPos(getOnPos()));
        }

        if (this.tickCount % 60 == 0)
        {
            try
            {
                entry.updateDimension(level.dimension());
                NbtIo.write(getPersistentData(), entry.getFile());
                ThutBot.saveBots();
            }
            catch (Exception e)
            {
                ThutBot.LOGGER.error("Error saving tag for {}", entry.name);
                ThutBot.LOGGER.error(e);
            }
        }
    }

    public void onChat(ServerChatEvent event)
    {
        ServerPlayer talker = event.getPlayer();
        if (talker instanceof BotPlayer) return;

        String cmd = event.getMessage();

        boolean isOrder = cmd.contains(this.getName().getString());

        // Decide if we want to say something back?
        if (!isOrder) return;

        PermNodes.registerNode(PERMBOTORDER, DefaultPermissionLevel.OP, "Allowed to give orders to thutbots");
        String s1 = "I Am A Bot";
        chat(s1);

        if (!PermNodes.getBooleanPerm(talker, PERMBOTORDER)) return;

        if (cmd.toLowerCase(Locale.ROOT).contains("where are you?"))
        {
            chat("I am at " + this.getOnPos());
            return;
        }

        if (cmd.toLowerCase(Locale.ROOT).contains("what are you doing?"))
        {
            if (routine == null) chat("I am idle");
            else chat("I am doing: " + routine.getKey());
            return;
        }

        Matcher startOrder = STARTORDER.matcher(cmd);

        boolean had = startOrder.find();

        if (!had)
        {
            startOrder = Pattern.compile("(build)(\\s)(\\w+:\\w+)").matcher(cmd);
            had = startOrder.find();
        }
        if (had)
        {
            String key = startOrder.group(3);
            IBotAI.Factory<?> factory = IBotAI.REGISTRY.get(key);
            if (factory != null)
            {
                s1 = "Starting " + key;
                this.getPersistentData().putString("ai_task", key);
                if (this.routine != null) this.routine.end(talker);
                this.routine = factory.create(this);
                routine.setKey(key);
                boolean valid = false;

                try
                {
                    valid = routine.init(cmd);
                }
                catch (Exception e)
                {
                    ThutBot.LOGGER.error(e);
                }

                if (!valid)
                {
                    chat("Invalid argument!");
                    this.getPersistentData().remove("ai_task");
                    this.routine = null;
                    return;
                }
                this.routine.start(talker);
                chat(s1);
            }
            else
            {
                s1 = "I don't know how to do that!";
                if (this.routine != null)
                {
                    this.routine.end(talker);
                    this.getPersistentData().remove("ai_task");
                    this.routine = null;
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
        else if (cmd.contains("reset"))
        {
            if (this.routine != null) this.routine.end(talker);
            List<String> tags = new ArrayList<>();
            tags.addAll(this.getPersistentData().getAllKeys());
            tags.forEach(s -> getPersistentData().remove(s));
            this.routine = null;
        }
    }

    public void chat(String message)
    {
        Component component = message.isEmpty() ? null
                : TComponent.translatable("chat.type.text", this.getDisplayName(), message);
        Component component1 = TComponent.translatable("chat.type.text", this.getDisplayName(), message);
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
