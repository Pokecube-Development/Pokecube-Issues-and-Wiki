package pokecube.core.client.gui;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent.RenderMoveMessages;

public class GuiInfoMessages
{
    private static final LinkedList<String> messages = Lists.newLinkedList();
    private static final LinkedList<String> recent   = Lists.newLinkedList();

    static long       time   = 0;
    static public int offset = 0;

    public static void addMessage(final ITextComponent message)
    {
        if (message == null)
        {
            PokecubeCore.LOGGER.warn("Null message was sent!", new NullPointerException());
            return;
        }
        PokecubeCore.LOGGER.debug("Recieved Message: " + message.getString());
        if (PokecubeCore.getConfig().battleLogInChat)
        {
            if (PokecubeCore.proxy.getPlayer() != null) PokecubeCore.proxy.getPlayer().sendMessage(message);
            return;
        }
        GuiInfoMessages.messages.push(message.getString());
        GuiInfoMessages.time = Minecraft.getInstance().player.ticksExisted;
        GuiInfoMessages.recent.addFirst(message.getString());
        if (GuiInfoMessages.messages.size() > 100) GuiInfoMessages.messages.remove(0);
    }

    public static void clear()
    {
        GuiInfoMessages.messages.clear();
        GuiInfoMessages.recent.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public static void draw(final RenderMoveMessages event)
    {
        if (PokecubeCore.getConfig().battleLogInChat) return;
        final Minecraft minecraft = Minecraft.getInstance();
        // TODO see about this?
        if (event.getType() == ElementType.CHAT && !(minecraft.currentScreen instanceof ChatScreen)) return;
        if (event.getType() != ElementType.CHAT && minecraft.currentScreen != null) return;

        GL11.glPushMatrix();
        final int texH = minecraft.fontRenderer.FONT_HEIGHT;
        final int trim = PokecubeCore.getConfig().messageWidth;
        final int paddingXPos = PokecubeCore.getConfig().messagePadding.get(0);
        final int paddingXNeg = PokecubeCore.getConfig().messagePadding.get(1);

        // TODO possbly fix lighitng here?
        final int[] mess = GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.getConfig().messageRef, PokecubeCore
                .getConfig().messagePos, new int[] { PokecubeCore.getConfig().messageWidth, 7
                        * minecraft.fontRenderer.FONT_HEIGHT }, (float) PokecubeCore.getConfig().messageSize);
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
        mx = minecraft.mouseHelper.getMouseX();
        my = minecraft.mouseHelper.getMouseY();

        if (minecraft.currentScreen != null)
        {
            i1 = (int) (mx * minecraft.currentScreen.width / minecraft.getMainWindow().getScaledWidth());
            j1 = (int) (minecraft.currentScreen.height - my * minecraft.currentScreen.height / minecraft.getMainWindow()
                    .getScaledHeight() - 1);
        }
        i1 = i1 - mess[0];
        j1 = j1 - mess[1];

        int w = 0;
        int h = 0;
        x = w;
        y = h;
        GL11.glTranslated(0, -texH * 7, 0);
        GL11.glTranslated(0, 0, 0);
        int num = -1;
        if (event.getType() == ElementType.CHAT)
        {
            num = 7;
            if (GuiInfoMessages.offset < 0) GuiInfoMessages.offset = 0;
            if (GuiInfoMessages.offset > GuiInfoMessages.messages.size() - 7)
                GuiInfoMessages.offset = GuiInfoMessages.messages.size() - 7;
        }
        else if (GuiInfoMessages.time > minecraft.player.ticksExisted - 30)
        {
            num = 6;
            GuiInfoMessages.offset = 0;
        }
        else
        {
            GuiInfoMessages.offset = 0;
            num = 6;
            GuiInfoMessages.time = minecraft.player.ticksExisted;
            if (!GuiInfoMessages.recent.isEmpty()) GuiInfoMessages.recent.removeLast();
        }
        while (GuiInfoMessages.recent.size() > 8)
            GuiInfoMessages.recent.removeLast();
        final List<String> toUse = num == 7 ? GuiInfoMessages.messages : GuiInfoMessages.recent;
        final int size = toUse.size() - 1;
        num = Math.min(num, size + 1);
        int shift = 0;
        for (int l = 0; l < num && shift < num; l++)
        {
            int index = l + GuiInfoMessages.offset;
            if (index < 0) index = 0;
            if (index > size) break;
            final String mess2 = toUse.get(index);
            final List<String> mess1 = minecraft.fontRenderer.listFormattedStringToWidth(mess2, trim);
            for (int j = mess1.size() - 1; j >= 0; j--)
            {
                h = y + texH * shift;
                w = x - trim;
                final int ph = 6 * texH - h;
                AbstractGui.fill(event.mat, w - paddingXNeg, ph, w + trim + paddingXPos, ph + texH, 0x66000000);
                minecraft.fontRenderer.drawString(event.mat, mess1.get(j), x - trim, ph, 0xffffff);
                if (j != 0) shift++;
            }
            shift++;
        }
        GL11.glPopMatrix();
    }
}
