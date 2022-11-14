package thut.api.util;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import thut.lib.TComponent;

public class PermNodes
{
    public static enum DefaultPermissionLevel
    {
        ALL, OP, NONE;

        public boolean matches(UUID player)
        {
            if (this == NONE) return false;
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            Optional<GameProfile> profile = server != null ? server.getProfileCache().get(player) : Optional.empty();
            boolean op = false;
            if (profile.isPresent())
            {
                op = server.getPlayerList().isOp(profile.get());
            }
            else
            {
                op = server.getPlayerList().isOp(testProfile);
            }
            return op ? true : this == ALL;
        }
    }

    public static class StringSetPermCache
    {
        private static class StringSetCache
        {
            private String value = "";
            private Set<String> values = Sets.newHashSet();

            public boolean contains(String input)
            {
                return values.contains(input);
            }

            private void setValue(String permission)
            {
                if (value.equals(permission)) return;
                value = permission;
                values.clear();
                var split = value.split(",");
                for (var s : split) values.add(s.strip());
            }
        }

        private static final Set<LoadingCache<?, ?>> CACHES = Sets.newHashSet();

        private final LoadingCache<ServerPlayer, StringSetCache> cache = CacheBuilder.newBuilder().maximumSize(100)
                .build(new CacheLoader<>()
                {
                    @Override
                    public StringSetCache load(ServerPlayer key) throws Exception
                    {
                        PermissionNode<String> node = PermNodes.getStringNode(StringSetPermCache.this.key);
                        StringSetCache ret = new StringSetCache();
                        ret.setValue(PermissionAPI.getPermission(key, node));
                        return ret;
                    }
                });

        private final String key;

        public StringSetPermCache(String key)
        {
            CACHES.add(cache);
            this.key = key;
        }

        public boolean contains(ServerPlayer player, String input)
        {
            try
            {
                StringSetCache cache = this.cache.get(player);
                PermissionNode<String> node = PermNodes.getStringNode(key);
                // This cache set does nothing if it hasn't changed, as the
                // cache handles that internally.
                cache.setValue(PermissionAPI.getPermission(player, node));
                return cache.contains(input);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static final GameProfile testProfile = new GameProfile(new UUID(1234567987, 123545787), "_permtest_");
    public static ServerPlayer testPlayer;

    private static final Map<String, PermissionNode<?>> NODES = Maps.newHashMap();
    private static final Map<String, StringSetPermCache> STRINGS = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static PermissionNode<Boolean> getBooleanNode(String name)
    {
        return (PermissionNode<Boolean>) NODES.get(name);
    }

    @SuppressWarnings("unchecked")
    public static PermissionNode<String> getStringNode(String name)
    {
        return (PermissionNode<String>) NODES.get(name);
    }

    public static StringSetPermCache getStringCache(String name)
    {
        return STRINGS.get(name);
    }

    public static boolean hasStringInList(ServerPlayer player, String perm, String value)
    {
        StringSetPermCache cache = PermNodes.getStringCache(perm);
        return cache.contains(player, value);
    }

    public static boolean getBooleanPerm(ServerPlayer player, String name)
    {
        PermissionNode<Boolean> node = getBooleanNode(name);
        return PermissionAPI.getPermission(player, node);
    }

    public static void registerBooleanNode(String modid, String name, DefaultPermissionLevel level, String message)
    {
        PermissionNode<Boolean> node = new PermissionNode<>(modid, name, PermissionTypes.BOOLEAN,
                (player, playerUUID, context) -> level.matches(playerUUID));
        node.setInformation(TComponent.literal(node.getNodeName()), TComponent.literal(message));
        NODES.put(name, node);
        NODES.put(node.getNodeName(), node);
    }

    public static void registerStringNode(String modid, String name, DefaultPermissionLevel level, String message,
            String _default)
    {
        PermissionNode<String> node = new PermissionNode<>(modid, name, PermissionTypes.STRING,
                (player, playerUUID, context) -> _default);
        node.setInformation(TComponent.literal(node.getNodeName()), TComponent.literal(message));
        NODES.put(name, node);
        NODES.put(node.getNodeName(), node);

        STRINGS.put(name, new StringSetPermCache(name));
        STRINGS.put(node.getNodeName(), new StringSetPermCache(node.getNodeName()));
    }

    public static void registerIntegerNode(String modid, String name, DefaultPermissionLevel level, String message,
            Integer _default)
    {
        PermissionNode<Integer> node = new PermissionNode<>(modid, name, PermissionTypes.INTEGER,
                (player, playerUUID, context) -> _default);
        node.setInformation(TComponent.literal(node.getNodeName()), TComponent.literal(message));
        NODES.put(name, node);
        NODES.put(node.getNodeName(), node);
    }

    public static void registerNode(PermissionNode<?> node)
    {
        NODES.put(node.getNodeName(), node);
    }

    public static void gatherPerms(PermissionGatherEvent.Nodes event)
    {
        StringSetPermCache.CACHES.forEach(e -> e.invalidateAll());
        Set<PermissionNode<?>> nodes = Sets.newHashSet();
        nodes.addAll(NODES.values());
        // We add them 1 at a time. For some reason, in the development
        // environment, this evnt is called 4 times in a row.
        nodes.forEach(node -> {
            if (!event.getNodes().contains(node)) event.addNodes(node);
        });
    }
}
