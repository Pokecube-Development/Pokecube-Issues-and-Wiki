package thut.bot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.api.entity.CopyCaps;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.IBotAI;

@Mod(value = "thutbot")
public class ThutBot
{
    public static final Logger LOGGER = LogManager.getLogger("thutbot");

    public ThutBot()
    {
        // Register our event listeners
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onChat);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onServerStart);

        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve("thutbot.log").toFile();
        if (logfile.exists())
        {
            FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").toFile().mkdirs();
            try
            {
                final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                Files.move(FMLPaths.GAMEDIR.get().resolve("logs").resolve("thutbot" + ".log"),
                        FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").resolve(String.format("%s_%s%s",
                                "thutbot", LocalDateTime.now().format(dtf).replace(":", "-"), ".log")));
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) ThutBot.LOGGER;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath())
                .setName("thutbot").build();
        logger.addAppender(appender);
        appender.start();

        PermissionAPI.registerNode(BotPlayer.PERMBOTORDER, DefaultPermissionLevel.OP,
                "Allowed to give orders to thutbots");

        IBotAI.MODULEPACKAGES.add(IBotAI.class.getPackageName());

        // The bot is technically a player, so we add this for MimicBot
        CopyCaps.register(EntityType.PLAYER);
    }

    // This ensures the bot is still alive and around
    private static void onServerTick(final ServerTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT) return;
        if (event.phase == Phase.START) return;
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        final UUID id = new UUID(2152346, 2344673);
        if (overworld.getPlayerByUUID(id) == null)
        {
            final GameProfile profile = new GameProfile(id, "ThutBot");
            final BotPlayer bot = new BotPlayer(overworld, profile);
            ThutBot.placeNewPlayer(server, bot.connection.connection, bot);
        }
    }

    // This is used to send commands to the bot
    private static void onChat(final ServerChatEvent chat)
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        server.tell(new TickTask(server.getTickCount() + 10, () -> {
            server.getPlayerList().getPlayers().forEach(p -> {
                if (p instanceof BotPlayer bot) bot.onChat(chat);
            });
        }));
    }

    private static void onServerStart(ServerAboutToStartEvent event)
    {
        IBotAI.init();
    }

    private static void placeNewPlayer(final MinecraftServer server, final Connection connection,
            final ServerPlayer player)
    {
        final PlayerList list = server.getPlayerList();
        final GameProfile gameprofile = player.getGameProfile();
        final GameProfileCache gameprofilecache = server.getProfileCache();
        final Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
        final String s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
        gameprofilecache.add(gameprofile);
        final CompoundTag compoundtag = list.load(player);
        @SuppressWarnings("deprecation")
        final ResourceKey<Level> resourcekey = compoundtag != null
                ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Dimension")))
                        .resultOrPartial(ThutBot.LOGGER::error).orElse(Level.OVERWORLD)
                : Level.OVERWORLD;
        final ServerLevel serverlevel = server.getLevel(resourcekey);
        ServerLevel serverlevel1;
        if (serverlevel == null)
        {
            ThutBot.LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourcekey);
            serverlevel1 = server.overworld();
        }
        else serverlevel1 = serverlevel;

        player.setLevel(serverlevel1);
        String s1 = "local";
        if (connection.getRemoteAddress() != null) s1 = connection.getRemoteAddress().toString();

        ThutBot.LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), s1,
                player.getId(), player.getX(), player.getY(), player.getZ());
        player.loadGameTypes(compoundtag);
        final ServerGamePacketListenerImpl servergamepacketlistenerimpl = player.connection;
        server.invalidateStatus();
        MutableComponent mutablecomponent;
        if (player.getGameProfile().getName().equalsIgnoreCase(s))
            mutablecomponent = new TranslatableComponent("multiplayer.player.joined", player.getDisplayName());
        else mutablecomponent = new TranslatableComponent("multiplayer.player.joined.renamed", player.getDisplayName(),
                s);

        list.broadcastMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
        servergamepacketlistenerimpl.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(),
                player.getXRot());
        list.addPlayer(player);
        list.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));

        for (final ServerPlayer element : list.getPlayers()) player.connection
                .send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, element));

        serverlevel1.addNewPlayer(player);
        serverlevel1.getChunkSource().move(player);
        server.getCustomBossEvents().onPlayerConnect(player);
        list.sendLevelInfo(player, serverlevel1);
        if (!server.getResourcePack().isEmpty()) player.sendTexturePack(server.getResourcePack(),
                server.getResourcePackHash(), server.isResourcePackRequired(), server.getResourcePackPrompt());

        for (final MobEffectInstance mobeffectinstance : player.getActiveEffects())
            servergamepacketlistenerimpl.send(new ClientboundUpdateMobEffectPacket(player.getId(), mobeffectinstance));

        if (compoundtag != null && compoundtag.contains("RootVehicle", 10))
        {
            final CompoundTag compoundtag1 = compoundtag.getCompound("RootVehicle");
            final Entity entity1 = EntityType.loadEntityRecursive(compoundtag1.getCompound("Entity"), serverlevel1,
                    (p_11223_) ->
                    {
                        return !serverlevel1.addWithUUID(p_11223_) ? null : p_11223_;
                    });
            if (entity1 != null)
            {
                UUID uuid;
                if (compoundtag1.hasUUID("Attach")) uuid = compoundtag1.getUUID("Attach");
                else uuid = null;

                if (entity1.getUUID().equals(uuid)) player.startRiding(entity1, true);
                else for (final Entity entity : entity1.getIndirectPassengers()) if (entity.getUUID().equals(uuid))
                {
                    player.startRiding(entity, true);
                    break;
                }

                if (!player.isPassenger())
                {
                    ThutBot.LOGGER.warn("Couldn't reattach entity to player");
                    entity1.discard();

                    for (final Entity entity2 : entity1.getIndirectPassengers()) entity2.discard();
                }
            }
        }
    }
}
