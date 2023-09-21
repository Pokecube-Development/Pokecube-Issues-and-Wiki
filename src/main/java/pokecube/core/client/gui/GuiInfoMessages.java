package pokecube.core.client.gui;

import java.util.LinkedList;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent.RenderMoveMessages;

public class GuiInfoMessages
{
    static final LinkedList<String> messages = Lists.newLinkedList();
    static final LinkedList<String> recent = Lists.newLinkedList();

    static long time = 0;
    static public int offset = 0;

    public static boolean fullDisplay()
    {
        return Minecraft.getInstance().screen instanceof ChatScreen;
    }

    public static void addMessage(final Component message)
    {
        if (message == null)
        {
            PokecubeAPI.LOGGER.warn("Null message was sent!", new NullPointerException());
            return;
        }
        if (PokecubeCore.getConfig().battleLogInChat)
        {
            if (PokecubeCore.proxy.getPlayer() != null)
                thut.lib.ChatHelper.sendSystemMessage(PokecubeCore.proxy.getPlayer(), message);;
            return;
        }
        GuiInfoMessages.messages.push(message.getString());
        GuiInfoMessages.time = Minecraft.getInstance().player.tickCount;
        GuiInfoMessages.recent.addFirst(message.getString());
        if (GuiInfoMessages.messages.size() > 100) GuiInfoMessages.messages.remove(0);
    }

    public static void clear()
    {
        GuiInfoMessages.messages.clear();
        GuiInfoMessages.recent.clear();
    }

    public static void draw(final RenderMoveMessages event)
    {
        if (PokecubeCore.getConfig().battleLogInChat) return;
        GuiDisplayPokecubeInfo.instance().drawMoveMessages(event);
    }
}
