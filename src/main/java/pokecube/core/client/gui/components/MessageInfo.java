package pokecube.core.client.gui.components;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.MutableComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiInfoMessages;
import thut.lib.TComponent;

public class MessageInfo extends GuiEventComponent
{
    public final LinkedList<String> messages = Lists.newLinkedList();
    public final LinkedList<String> recent = Lists.newLinkedList();

    public long time = 0;
    public int offset = 0;

    @Override
    protected void onMovedGui()
    {
        PokecubeCore.getConfig().guiMessagePos.set(0, this.bounds.x0);
        PokecubeCore.getConfig().guiMessagePos.set(1, this.bounds.y0);
        super.onMovedGui();
    }

    @Override
    protected void preDraw(GuiEvent event)
    {
        if (PokecubeCore.getConfig().battleLogInChat) return;
        final Minecraft minecraft = Minecraft.getInstance();
        final boolean inChat = GuiInfoMessages.fullDisplay();
        final int texH = minecraft.font.lineHeight;
        int num = inChat ? 8 : 6;
        if (clickA == 0)
        {
            int x0 = PokecubeCore.getConfig().guiMessagePos.get(0);
            int y0 = PokecubeCore.getConfig().guiMessagePos.get(1);
            int w = PokecubeCore.getConfig().messageWidth;
            if (texH * num != this.bounds.h) this.bounds.setBox(x0, y0, w, texH * num);
            this.ref = PokecubeCore.getConfig().messageRef;
        }
    }

    @Override
    public void _drawGui(GuiEvent event)
    {
        if (PokecubeCore.getConfig().battleLogInChat) return;
        final Minecraft minecraft = Minecraft.getInstance();

        event.getMat().pushPose();
        event.getMat().translate(this.pos.x0, this.pos.y0, 0);

        final int texH = minecraft.font.lineHeight;
        final int trim = PokecubeCore.getConfig().messageWidth;
        final int paddingXPos = PokecubeCore.getConfig().messagePadding.get(0);
        final int paddingXNeg = PokecubeCore.getConfig().messagePadding.get(1);

        int x = 0, y = 0;

        int w = 0;
        int h = 0;
        x = w;
        y = h;
        event.getMat().translate(this.pos.w, texH, 0);
        int num = -1;
        final boolean inChat = GuiInfoMessages.fullDisplay();

        if (inChat)
        {
            num = 7;
            if (this.offset < 0) this.offset = 0;
            if (this.offset > this.messages.size() - 7) this.offset = this.messages.size() - 7;
        }
        else if (this.time > minecraft.player.tickCount - 30)
        {
            num = 6;
            this.offset = 0;
        }
        else
        {
            this.offset = 0;
            num = 6;
            this.time = minecraft.player.tickCount;
            if (!this.recent.isEmpty()) this.recent.removeLast();
        }
        while (this.recent.size() > 8) this.recent.removeLast();
        final List<String> toUse = num == 7 ? this.messages : this.recent;
        final int size = toUse.size() - 1;
        num = Math.min(num, size + 1);
        int shift = 0;
        for (int l = 0; l < num && shift < num; l++)
        {
            int index = l + this.offset;
            if (index < 0) index = 0;
            if (index > size) break;
            final MutableComponent mess2 = TComponent.literal(toUse.get(index));
            var mess1 = minecraft.font.split(mess2, trim);
            for (int j = mess1.size() - 1; j >= 0; j--)
            {
                h = y + texH * shift;
                w = x - trim;
                final int ph = 6 * texH - h;
                GuiComponent.fill(event.getMat(), w - paddingXNeg, ph, w + trim + paddingXPos, ph + texH, 0x66000000);
                minecraft.font.draw(event.getMat(), mess1.get(j), x - trim, ph, 0xffffff);
                if (j != 0) shift++;
            }
            shift++;
        }
        event.getMat().popPose();
    }

}