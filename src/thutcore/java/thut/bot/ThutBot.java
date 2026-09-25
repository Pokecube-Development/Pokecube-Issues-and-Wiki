package thut.bot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import thut.api.util.JsonUtil;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.IBotAI;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

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

@Mod(value = "thutcore")
public class ThutBot
{
    public static final Logger LOGGER = LogManager.getLogger("thutbot");

    public static final UUID BOTMODID = new UUID(Hashing.goodFastHash(64).hashUnencodedChars("thutbot").padToLong(), 0);

    public ThutBot()
    {
        // Register our event listeners
        ThutCore.FORGE_BUS.addListener(ThutBot::onServerTick);
        ThutCore.FORGE_BUS.addListener(ThutBot::onChat);
        ThutCore.FORGE_BUS.addListener(ThutBot::onServerStart);
        ThutCore.FORGE_BUS.addListener(ThutBot::onServerStop);
        ThutCore.FORGE_BUS.addListener(ThutBot::onCommandRegister);

        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve("thutbot.log").toFile();
        if (logfile.exists())
        {
            FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").toFile().mkdirs();
            try
            {
                final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                Files.move(FMLPaths.GAMEDIR.get().resolve("logs").resolve("thutbot" + ".log"),
                        FMLPaths.GAMEDIR.get().resolve("logs").resolve("old").resolve(
                                String.format("%s_%s%s", "thutbot", LocalDateTime.now().format(dtf).replace(":", "-"),
                                        ".log")));
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

        PermNodes.registerBooleanNode(ThutCore.MODID, BotPlayer.PERMBOTORDER, DefaultPermissionLevel.OP,
                "Allowed to give orders to thutbots");

        IBotAI.MODULEPACKAGES.add(IBotAI.class.getPackageName());
    }

    private static final ArrayList<BotEntry> ALL_BOTS = Lists.newArrayList();
    public static Map<UUID, BotEntry> BOT_MAP = Maps.newHashMap();

    public static final String PERMBOT = "thutbot.perm";
    public static final String PERMBOTSUMMON = "thutbot.perm.summon";
    public static final String PERMBOTKILL = "thutbot.perm.kill";

    static
    {
        PermNodes.registerBooleanNode(ThutCore.MODID, PERMBOT, DefaultPermissionLevel.OP,
                "Allowed to use base bot commants");
        PermNodes.registerBooleanNode(ThutCore.MODID, PERMBOTSUMMON, DefaultPermissionLevel.OP,
                "Allowed to make a new thutbot");
        PermNodes.registerBooleanNode(ThutCore.MODID, PERMBOTKILL, DefaultPermissionLevel.OP,
                "Allowed to remove a thutbot");
    }

    private static final SimpleCommandExceptionType NO_SUMMON_2 = new SimpleCommandExceptionType(
            Component.translatable("Cannot summon a second bot of the same name!"));

    private static final SimpleCommandExceptionType NO_KILL = new SimpleCommandExceptionType(
            Component.translatable("No bot by that name to kill!"));

    private static void onCommandRegister(final RegisterCommandsEvent event)
    {
        final LiteralArgumentBuilder<CommandSourceStack> command_base = Commands.literal("thutbot").requires(s -> {
            if (!(s.getEntity() instanceof ServerPlayer player)) return true;
            return PermNodes.getBooleanPerm(player, PERMBOT);
        });

        final LiteralArgumentBuilder<CommandSourceStack> summon_bot = command_base.then(
                Commands.literal("summon").requires(s -> {
                    if (!(s.getEntity() instanceof ServerPlayer player)) return true;
                    return PermNodes.getBooleanPerm(player, PERMBOTSUMMON);
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

                        if (!entry.hidden) ThutBot.placeNewPlayer(server, bot.connection.getConnection(), bot);
                        else server.overworld().addFreshEntity(bot);

                        entry._profile = bot.getGameProfile();
                        saveBots();
                    }
                    else
                    {
                        throw NO_SUMMON_2.create();
                    }
                    return 0;
                })));

        final LiteralArgumentBuilder<CommandSourceStack> kill_bot = command_base.then(
                Commands.literal("kill").requires(s -> {
                    if (!(s.getEntity() instanceof ServerPlayer player)) return true;
                    return PermNodes.getBooleanPerm(player, PERMBOTKILL);
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
    private static void onServerTick(final ServerTickEvent.Post event)
    {
        final MinecraftServer server = event.getServer();
        if (server.getTickCount() % 200 != 0) return;

        ServerLevel level = server.overworld();

        for (BotEntry p : ALL_BOTS)
        {
            final BotPlayer bot = new BotPlayer(level, p.getProfile());
            if (p.hidden)
            {
                ServerLevel world = server.getLevel(p.getDimension());
                if (world != null && world.getPlayerByUUID(p.getProfile().getId()) == null)
                {
                    world.addFreshEntity(bot);
                }
            }
            else if (server.getPlayerList().getPlayer(p.getProfile().getId()) == null)
            {
                ThutBot.placeNewPlayer(server, bot.connection.getConnection(), bot);
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
        public boolean hidden = false;
        public String dimension = "minecraft:overworld";
        public String name;
        GameProfile _profile;
        File _file;
        ResourceKey<Level> _dimension;

        private void initProfile()
        {
            long hash = Hashing.goodFastHash(64).hashUnencodedChars(name).padToLong();
            final UUID id = new UUID(hash, hash);
            this._profile = new GameProfile(id, name);
        }

        public ResourceKey<Level> getDimension()
        {
            if (_dimension == null || !_dimension.location().toString().equals(dimension))
            {
                _dimension = ResourceKey.create(RegHelper.DIMENSION_REGISTRY, ResourceLocation.parse(dimension));
            }
            return _dimension;
        }

        public void updateDimension(ResourceKey<Level> level)
        {
            if (level != _dimension)
            {
                _dimension = level;
                dimension = level.location().toString();
            }
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
        var list = server.getPlayerList();
        GameProfile gameprofile = player.getGameProfile();
        GameProfileCache gameprofilecache = server.getProfileCache();
        String s;
        if (gameprofilecache != null)
        {
            Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
            s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
            gameprofilecache.add(gameprofile);
        }
        else
        {
            s = gameprofile.getName();
        }

        Optional<CompoundTag> optional1 = list.load(player);
        ResourceKey<Level> resourcekey = optional1.<ResourceKey<Level>>flatMap(
                p_337568_ -> DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, p_337568_.get("Dimension")))
                        .resultOrPartial(LOGGER::error)).orElse(Level.OVERWORLD);
        ServerLevel serverlevel = server.getLevel(resourcekey);
        ServerLevel serverlevel1;
        if (serverlevel == null)
        {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", resourcekey);
            serverlevel1 = server.overworld();
        }
        else
        {
            serverlevel1 = serverlevel;
        }

        player.setServerLevel(serverlevel1);
        String s1 = connection.getLoggableAddress(server.logIPs());
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName().getString(), s1,
                player.getId(), player.getX(), player.getY(), player.getZ());
        player.loadGameTypes(optional1.orElse(null));

        player.getStats().markAllDirty();
        player.getRecipeBook().sendInitialRecipeBook(player);
        server.invalidateStatus();
        MutableComponent mutablecomponent;
        if (player.getGameProfile().getName().equalsIgnoreCase(s))
        {
            mutablecomponent = Component.translatable("multiplayer.player.joined", player.getDisplayName());
        }
        else
        {
            mutablecomponent = Component.translatable("multiplayer.player.joined.renamed", player.getDisplayName(), s);
        }

        list.broadcastSystemMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), false);
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        List<ServerPlayer> players = ObfuscationReflectionHelper.getPrivateValue(PlayerList.class, list, "players");
        Map<UUID, ServerPlayer> playerMap = ObfuscationReflectionHelper.getPrivateValue(PlayerList.class, list,
                "playersByUUID");
        players.add(player);
        playerMap.put(player.getUUID(), player);

        list.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
        list.sendLevelInfo(player, serverlevel1);
        serverlevel1.addNewPlayer(player);
        server.getCustomBossEvents().onPlayerConnect(player);
        if (optional1.isPresent() && optional1.get().contains("RootVehicle", 10))
        {
            CompoundTag compoundtag = optional1.get().getCompound("RootVehicle");
            Entity entity = EntityType.loadEntityRecursive(compoundtag.getCompound("Entity"), serverlevel1,
                    p_215603_ -> !serverlevel1.addWithUUID(p_215603_) ? null : p_215603_);
            if (entity != null)
            {
                UUID uuid;
                if (compoundtag.hasUUID("Attach"))
                {
                    uuid = compoundtag.getUUID("Attach");
                }
                else
                {
                    uuid = null;
                }

                if (entity.getUUID().equals(uuid))
                {
                    player.startRiding(entity, true);
                }
                else
                {
                    for (Entity entity1 : entity.getIndirectPassengers())
                    {
                        if (entity1.getUUID().equals(uuid))
                        {
                            player.startRiding(entity1, true);
                            break;
                        }
                    }
                }

                if (!player.isPassenger())
                {
                    LOGGER.warn("Couldn't reattach entity to player");
                    entity.discard();

                    for (Entity entity2 : entity.getIndirectPassengers())
                    {
                        entity2.discard();
                    }
                }
            }
        }
        player.initInventoryMenu();
        net.neoforged.neoforge.event.EventHooks.firePlayerLoggedIn(player);
    }
}
