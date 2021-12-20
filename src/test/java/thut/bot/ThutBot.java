package thut.bot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import thut.api.entity.CopyCaps;
import thut.api.util.JsonUtil;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.IBotAI;

@Mod(value = "thutbot")
public class ThutBot
{
    public static final Logger LOGGER = LogManager.getLogger("thutbot");

    public static final UUID BOTMODID = new UUID(Hashing.goodFastHash(64).hashUnencodedChars("thutbot").padToLong(), 0);

    public ThutBot()
    {
        // Register our event listeners
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onChat);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onServerStop);
        MinecraftForge.EVENT_BUS.addListener(ThutBot::onCommandRegister);

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

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));

    }

    private static ArrayList<BotEntry> ALL_BOTS = Lists.newArrayList();
    public static Map<UUID, BotEntry> BOT_MAP = Maps.newHashMap();

    public static final String PERMBOT = "thutbot.perm";
    public static final String PERMBOTSUMMON = "thutbot.perm.summon";
    public static final String PERMBOTKILL = "thutbot.perm.kill";

    private static final SimpleCommandExceptionType NO_SUMMON_2 = new SimpleCommandExceptionType(
            new TranslatableComponent("Cannot summon a second bot of the same name!"));

    private static final SimpleCommandExceptionType NO_KILL = new SimpleCommandExceptionType(
            new TranslatableComponent("No bot by that name to kill!"));

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        PermissionAPI.registerNode(PERMBOT, DefaultPermissionLevel.OP, "Allowed to use base bot commants");
        PermissionAPI.registerNode(PERMBOTSUMMON, DefaultPermissionLevel.OP, "Allowed to make a new thutbot");
        PermissionAPI.registerNode(PERMBOTKILL, DefaultPermissionLevel.OP, "Allowed to remove a thutbot");

        final LiteralArgumentBuilder<CommandSourceStack> command_base = Commands.literal("thutbot").requires(s -> {
            if (!(s.getEntity() instanceof ServerPlayer player)) return true;
            return PermissionAPI.hasPermission(player, PERMBOT);
        });

        final LiteralArgumentBuilder<CommandSourceStack> summon_bot = command_base
                .then(Commands.literal("summon").requires(s ->
                {
                    if (!(s.getEntity() instanceof ServerPlayer player)) return true;
                    return PermissionAPI.hasPermission(player, PERMBOTSUMMON);
                }).then(Commands.argument("name", StringArgumentType.string()).executes(ctx -> {
                    final String name = StringArgumentType.getString(ctx, "name");

                    ServerLevel level = ctx.getSource().getLevel();
                    MinecraftServer server = ctx.getSource().getServer();

                    BotEntry entry = new BotEntry();
                    entry.name = name;

                    if (server.getPlayerList().getPlayer(entry.getProfile().getId()) == null)
                    {
                        ALL_BOTS.add(entry);
                        BOT_MAP.put(entry.getProfile().getId(), entry);
                        final BotPlayer bot = new BotPlayer(level, entry.getProfile());
                        ThutBot.placeNewPlayer(server, bot.connection.connection, bot);
                        entry._profile = bot.getGameProfile();
                        saveBots();
                    }
                    else
                    {
                        throw NO_SUMMON_2.create();
                    }
                    return 0;
                })));

        final LiteralArgumentBuilder<CommandSourceStack> kill_bot = command_base
                .then(Commands.literal("kill").requires(s ->
                {
                    if (!(s.getEntity() instanceof ServerPlayer player)) return true;
                    return PermissionAPI.hasPermission(player, PERMBOTKILL);
                }).then(Commands.argument("name", StringArgumentType.string()).executes(ctx -> {
                    final String name = StringArgumentType.getString(ctx, "name");
                    MinecraftServer server = ctx.getSource().getServer();
                    if (server.getPlayerList().getPlayerByName(name) instanceof BotPlayer bot)
                    {
                        ALL_BOTS.removeIf(e -> e.name.equals(name));
                        BOT_MAP.remove(bot.getUUID());
                        server.getPlayerList().remove(bot);
                        saveBots();
                    }
                    else
                    {
                        throw NO_KILL.create();
                    }
                    return 0;
                })));
        event.getDispatcher().register(summon_bot);
        event.getDispatcher().register(kill_bot);
    }

    public static void saveBots()
    {
        final Path dir = FMLPaths.CONFIGDIR.get().resolve("thutbot");
        dir.toFile().mkdirs();
        File file = dir.resolve("thutbots.json").toFile();

        BotList list = new BotList();
        ALL_BOTS.forEach(g -> list.bots.add(g));

        try
        {
            FileUtils.writeStringToFile(file, JsonUtil.gson.toJson(list), "UTF-8");
        }
        catch (IOException e)
        {
            LOGGER.error("Error saving default bot list", e);
        }
    }

    // This ensures the bot is still alive and around
    private static void onServerTick(final ServerTickEvent event)
    {
        if (event.side == LogicalSide.CLIENT) return;
        if (event.phase == Phase.START) return;
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server.getTickCount() % 200 != 0) return;

        ServerLevel level = server.overworld();

        for (int i = 0; i < ALL_BOTS.size(); i++)
        {
            BotEntry p = ALL_BOTS.get(i);
            if (server.getPlayerList().getPlayer(p.getProfile().getId()) == null)
            {
                final BotPlayer bot = new BotPlayer(level, p.getProfile());
                ThutBot.placeNewPlayer(server, bot.connection.connection, bot);
            }
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

    public static class BotEntry
    {
        public String name;
        GameProfile _profile;
        File _file;

        private void initProfile()
        {
            long hash = Hashing.goodFastHash(64).hashUnencodedChars(name).padToLong();
            final UUID id = new UUID(hash, hash);
            this._profile = new GameProfile(id, name);
        }

        public GameProfile getProfile()
        {
            if (_profile == null) initProfile();
            return _profile;
        }

        public File getFile()
        {
            if (_file == null)
            {
                final Path dir = FMLPaths.CONFIGDIR.get().resolve("thutbot");
                dir.toFile().mkdirs();
                _file = dir.resolve(this.name + ".dat").toFile();
            }
            return _file;
        }
    }

    public static class BotList
    {
        public List<BotEntry> bots = Lists.newArrayList();
    }

    private static void onServerStart(ServerAboutToStartEvent event)
    {
        IBotAI.init();

        final Path dir = FMLPaths.CONFIGDIR.get().resolve("thutbot");
        dir.toFile().mkdirs();
        File file = dir.resolve("thutbots.json").toFile();

        if (!file.exists())
        {
            saveBots();
        }
        else
        {
            // We load the bots
            try
            {
                ALL_BOTS.clear();
                String json = FileUtils.readFileToString(file, "UTF-8");
                BotList loaded = JsonUtil.gson.fromJson(json, BotList.class);
                loaded.bots.forEach(entry -> {
                    ALL_BOTS.add(entry);
                    BOT_MAP.put(entry.getProfile().getId(), entry);
                });
            }
            catch (Exception e)
            {
                LOGGER.error("Error loading saved bot list", e);
            }
        }
    }

    private static void onServerStop(ServerStoppedEvent event)
    {
        saveBots();
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

        Map<UUID, ServerPlayer> playerMap = ObfuscationReflectionHelper.getPrivateValue(PlayerList.class, list,
                "f_11197_");
        playerMap.put(player.getUUID(), player);

        list.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, player));

        serverlevel1.addNewPlayer(player);
        serverlevel1.getChunkSource().move(player);
        server.getCustomBossEvents().onPlayerConnect(player);
        list.sendLevelInfo(player, serverlevel1);

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
