package thut.lib;

import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ChatHelper
{
    public static void sendSystemMessage(Player player, Component message)
    {
        if (player != null) player.displayClientMessage(message, false);
    }

    public static void sendSystemMessage(Player player, Component message, UUID who)
    {
        sendSystemMessage(player, message);
    }
}
