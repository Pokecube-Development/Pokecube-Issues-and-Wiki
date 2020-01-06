package pokecube.core.client.gui.watch.progress;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.ITextComponent;
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

    public Progress(final ITextComponent title, final GuiPokeWatch watch)
    {
        super(title, watch);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 30;
        int dy = 0;
        final int colour = 0xFFFFFFFF;
        for (final String s : this.lines)
        {
            this.drawCenteredString(this.font, s, x, y + dy, colour);
            dy += this.font.FONT_HEIGHT;
            if (s.isEmpty()) dy -= this.font.FONT_HEIGHT / 1.5f;
        }
        super.render(mouseX, mouseY, partialTicks);
    }

}
