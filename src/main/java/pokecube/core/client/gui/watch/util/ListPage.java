package pokecube.core.client.gui.watch.util;

import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class ListPage<T extends AbstractList.AbstractListEntry<T>> extends WatchPage
{
    protected ScrollGui<T> list;
    /**
     * Set this to true if the page handles rendering the list itself.
     */
    protected boolean      handlesList = false;

    public ListPage(final ITextComponent title, final GuiPokeWatch watch)
    {
        super(title, watch);
    }

    public void drawTitle(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        this.drawCenteredString(this.font, this.getTitle().getFormattedText(), x, y, 0xFFFFFFFF);
    }

    @Override
    public void init()
    {
        super.init();
        this.initList();
    }

    public void initList()
    {
        if (this.list != null) this.children.remove(this.list);
    }

    @Override
    public void onPageOpened()
    {
        this.initList();
        super.onPageOpened();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawTitle(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
        // Draw the list
        if (!this.handlesList) this.list.render(mouseX, mouseY, partialTicks);
    }
}
