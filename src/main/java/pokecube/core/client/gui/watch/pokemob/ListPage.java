package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.gui.widget.list.AbstractList;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;

public abstract class ListPage<T extends AbstractList.AbstractListEntry<T>> extends PokeInfoPage
{
    ScrollGui<T> list;

    public ListPage(final PokemobInfoPage parent, final String title)
    {
        super(parent, title);
    }

    @Override
    void drawInfo(final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    protected void drawTitle(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        this.drawCenteredString(this.font, this.getTitle().getString(), x, y, 0xFFFFFFFF);
    }

    @Override
    public void init()
    {
        super.init();
        this.initList();
        this.children.add(this.list);
    }

    public void initList()
    {
        if (this.list != null) this.children.remove(this.list);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        this.initList();
        this.children.add(this.list);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(mouseX, mouseY, partialTicks);
        this.list.render(mouseX, mouseY, partialTicks);
    }

}
