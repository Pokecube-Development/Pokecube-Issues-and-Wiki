package pokecube.core.client.gui;

import com.google.common.collect.Lists;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent.RenderMoveMessages;
import thut.lib.TComponent;

public class GuiInfoMessages
{
    private static final LinkedList<String> messages = Lists.newLinkedList();
    private static final LinkedList<String> recent = Lists.newLinkedList();

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

    public static void draw(final RenderMoveMessages event, GuiGraphics graphics)
    {
        if (PokecubeCore.getConfig().battleLogInChat) return;
        final Minecraft minecraft = Minecraft.getInstance();

        event.getMat().pushPose();
        final int texH = minecraft.font.lineHeight;
        final int trim = PokecubeCore.getConfig().messageWidth;
        final int paddingXPos = PokecubeCore.getConfig().messagePadding.get(0);
        final int paddingXNeg = PokecubeCore.getConfig().messagePadding.get(1);

        final int[] mess = GuiDisplayPokecubeInfo.applyTransform(event.getMat(), PokecubeCore.getConfig().messageRef,
                PokecubeCore.getConfig().messagePos, new int[]
                { PokecubeCore.getConfig().messageWidth, 7 * minecraft.font.lineHeight },
                (float) PokecubeCore.getConfig().messageSize);
        int x = 0, y = 0;
        final float s = (float) PokecubeCore.getConfig().messageSize;
        x = x - 150;
        final Rectangle messRect = new Rectangle(x, y - 7 * texH, 150, 8 * texH);
        x += mess[2];
        y += mess[3];
        messRect.setBounds((int) (x * s), (int) ((y - 7 * texH) * s), (int) (150 * s), (int) (8 * texH * s));

        int i1 = -10;
        int j1 = -10;

        double mx, my;
        mx = minecraft.mouseHandler.xpos();
        my = minecraft.mouseHandler.ypos();

        if (minecraft.screen != null)
        {
            i1 = (int) (mx * minecraft.screen.width / minecraft.getWindow().getGuiScaledWidth());
            j1 = (int) (minecraft.screen.height
                    - my * minecraft.screen.height / minecraft.getWindow().getGuiScaledHeight() - 1);
        }
        i1 = i1 - mess[0];
        j1 = j1 - mess[1];

        int w = 0;
        int h = 0;
        x = w;
        y = h;
        event.getMat().translate(0, -texH * 7, 0);
        int num = -1;
        final boolean inChat = fullDisplay();

        if (inChat)
        {
            num = 7;
            if (GuiInfoMessages.offset < 0) GuiInfoMessages.offset = 0;
            if (GuiInfoMessages.offset > GuiInfoMessages.messages.size() - 7)
                GuiInfoMessages.offset = GuiInfoMessages.messages.size() - 7;
        }
        else if (GuiInfoMessages.time > minecraft.player.tickCount - 30)
        {
            num = 6;
            GuiInfoMessages.offset = 0;
        }
        else
        {
            GuiInfoMessages.offset = 0;
            num = 6;
            GuiInfoMessages.time = minecraft.player.tickCount;
            if (!GuiInfoMessages.recent.isEmpty()) GuiInfoMessages.recent.removeLast();
        }
        while (GuiInfoMessages.recent.size() > 8) GuiInfoMessages.recent.removeLast();
        final List<String> toUse = num == 7 ? GuiInfoMessages.messages : GuiInfoMessages.recent;
        final int size = toUse.size() - 1;
        num = Math.min(num, size + 1);
        int shift = 0;
        for (int l = 0; l < num && shift < num; l++)
        {
            int index = l + GuiInfoMessages.offset;
            if (index < 0) index = 0;
            if (index > size) break;
            final MutableComponent mess2 = TComponent.literal(toUse.get(index));
            var mess1 = minecraft.font.split(mess2, trim);
            for (int j = mess1.size() - 1; j >= 0; j--)
            {
                h = y + texH * shift;
                w = x - trim;
                final int ph = 6 * texH - h;
                // TODO: Check this
                graphics.fill(w - paddingXNeg, ph, w + trim + paddingXPos, ph + texH, 0x66000000);
                // minecraft.font.draw(event.getMat(), mess1.get(j), x - trim, ph, 0xffffff);
                if (j != 0) shift++;
            }
            shift++;
        }
        event.getMat().popPose();
    }
}
