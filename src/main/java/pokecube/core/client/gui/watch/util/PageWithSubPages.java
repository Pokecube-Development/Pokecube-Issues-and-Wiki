package pokecube.core.client.gui.watch.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class PageWithSubPages<T extends WatchPage> extends WatchPage
{
    protected T   current_page;
    protected int index = 0;

    public PageWithSubPages(final ITextComponent title, final GuiPokeWatch watch)
    {
        super(title, watch);
    }

    public void changePage(final int newIndex)
    {
        this.closeSubPage();
        this.index = newIndex;
        if (this.index < 0) this.index = this.pageCount() - 1;
        if (this.index > this.pageCount() - 1) this.index = 0;
        this.openSubPage();
    }

    protected void closeSubPage()
    {
        this.current_page.onPageClosed();
        this.children().remove(this.current_page);
    }

    protected abstract T createPage(int index);

    @Override
    public void onPageClosed()
    {
        this.preSubClosed();
        try
        {
            if (this.current_page != null) this.current_page.onPageClosed();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
        }
        super.onPageClosed();
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        this.preSubOpened();
        this.openSubPage();
    }

    protected void openSubPage()
    {
        this.current_page = this.createPage(this.index);
        try
        {
            this.current_page.init();
            this.current_page.onPageOpened();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
        }
        @SuppressWarnings("unchecked")
        final List<IGuiEventListener> list = (List<IGuiEventListener>) this.children();
        list.add(this.current_page);
    }

    protected abstract int pageCount();

    public void postPageDraw(final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    public void prePageDraw(final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    public void preSubClosed()
    {

    }

    public void preSubOpened()
    {

    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.font == null) this.font = Minecraft.getInstance().fontRenderer;
        this.prePageDraw(mouseX, mouseY, partialTicks);
        this.current_page.render(mouseX, mouseY, partialTicks);
        this.postPageDraw(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void resize(final Minecraft p_resize_1_, final int p_resize_2_, final int p_resize_3_)
    {
        this.closeSubPage();
        super.resize(p_resize_1_, p_resize_2_, p_resize_3_);
    }

}
