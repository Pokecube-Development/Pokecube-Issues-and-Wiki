package pokecube.core.utils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import pokecube.core.PokecubeCore;

@Mod.EventBusSubscriber
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

    public static final GameProfile testProfile = new GameProfile(new UUID(1234567987, 123545787), "_permtest_");
    public static ServerPlayer testPlayer;

    private static final Map<String, PermissionNode<?>> NODES = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static PermissionNode<Boolean> getBooleanNode(String name)
    {
        return (PermissionNode<Boolean>) NODES.get(name);
    }

    @SuppressWarnings("unchecked")
    public static boolean getBooleanPerm(ServerPlayer player, String name)
    {
        PermissionNode<Boolean> node = getBooleanNode(name);
        return PermissionAPI.getPermission(player, node);
    }

    public static void registerNode(String name, DefaultPermissionLevel level, String message)
    {
        PermissionNode<Boolean> node = new PermissionNode<>(PokecubeCore.MODID, name, PermissionTypes.BOOLEAN,
                (player, playerUUID, context) -> level.matches(playerUUID));
        node.setInformation(new TextComponent(node.getNodeName()), new TextComponent(message));

        if (NODES.containsKey(name) || NODES.containsKey(node.getNodeName()))
        {
            // We just skip here, this happens as forge re-loads things 4 times
            // during the game's startup...
        }
        else
        {
            NODES.put(name, node);
            NODES.put(node.getNodeName(), node);
        }
    }

    public static void registerNode(PermissionNode<?> node)
    {
        NODES.put(node.getNodeName(), node);
    }

    public static long test = 0;

    @SubscribeEvent
    public static void gatherPerms(PermissionGatherEvent.Nodes event)
    {
        Permissions.register();
        Set<PermissionNode<?>> nodes = Sets.newHashSet();
        nodes.addAll(NODES.values());
        
        // FIXME do this with the single call when it stops crashing.
        // event.addNodes(nodes);
        
        // We add them 1 at a time. For some reason, in the development
        // environment, this evnt is called 4 times in a row.
        nodes.forEach(node -> {
            if (!event.getNodes().contains(node)) event.addNodes(node);
        });
    }
}