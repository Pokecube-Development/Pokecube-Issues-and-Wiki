package pokecube.core.client.gui.watch.pokemob;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;

public abstract class ListPage<T extends AbstractSelectionList.Entry<T>> extends PokeInfoPage
{
    ScrollGui<T> list;

    public ListPage(final PokemobInfoPage parent, final String title, final ResourceLocation day, final ResourceLocation night)
    {
        super(parent, title, day, night);
    }

    @Override
    void drawInfo(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {

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
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.list.render(graphics, mouseX, mouseY, partialTicks);
    }

}
