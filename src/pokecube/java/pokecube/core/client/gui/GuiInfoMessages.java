package pokecube.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;

public class GuiInfoMessages
{
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
        GuiDisplayPokecubeInfo.messageRenderer.messages.push(message.getString());
        GuiDisplayPokecubeInfo.messageRenderer.time = Minecraft.getInstance().player.tickCount;
        GuiDisplayPokecubeInfo.messageRenderer.recent.addFirst(message.getString());
        if (GuiDisplayPokecubeInfo.messageRenderer.messages.size() > 100)
            GuiDisplayPokecubeInfo.messageRenderer.messages.remove(0);
    }

    public static void clear()
    {
        GuiDisplayPokecubeInfo.messageRenderer.messages.clear();
        GuiDisplayPokecubeInfo.messageRenderer.recent.clear();
    }
}
