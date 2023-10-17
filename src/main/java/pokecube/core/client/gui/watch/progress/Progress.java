package pokecube.core.client.gui.watch.progress;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.util.WatchPage;

public abstract class Progress extends WatchPage
{
    protected int caught0;
    protected int caught1;
    protected int hatched0;
    protected int hatched1;
    protected int killed0;
    protected int killed1;

    protected List<String> lines = Lists.newArrayList();

    public Progress(final Component title, final GuiPokeWatch watch)
    {
        super(title, watch, GuiPokeWatch.TEX_DM, GuiPokeWatch.TEX_NM);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2; // +80
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2; // +30
        int dy = 50;
        final int dx = 130;
        final int colour = 0x336633;
        for (final String s : this.lines)
        {
            graphics.drawString(this.font, s, x + dx - this.font.width(s) / 2, y + dy, colour, false);
            dy += this.font.lineHeight;
            if (s.isEmpty()) dy -= this.font.lineHeight / 1.25f;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

}
