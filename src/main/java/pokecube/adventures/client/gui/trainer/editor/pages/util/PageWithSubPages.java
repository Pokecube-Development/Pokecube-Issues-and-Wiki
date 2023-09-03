package pokecube.adventures.client.gui.trainer.editor.pages.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.api.PokecubeAPI;

public abstract class PageWithSubPages<T extends Page> extends Page
{
    protected T   current_page;
    protected int index = 0;

    public PageWithSubPages(final Component title, final EditorGui parent)
    {
        super(title, parent);
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
            PokecubeAPI.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
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
            PokecubeAPI.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
        }
        @SuppressWarnings("unchecked")
        final List<GuiEventListener> list = (List<GuiEventListener>) this.children();
        list.add(this.current_page);
    }

    protected abstract int pageCount();

    public void postPageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    public void prePageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    public void preSubClosed()
    {

    }

    public void preSubOpened()
    {

    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.font == null) this.font = Minecraft.getInstance().font;
        this.prePageDraw(graphics, mouseX, mouseY, partialTicks);
        this.current_page.render(graphics, mouseX, mouseY, partialTicks);
        this.postPageDraw(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void resize(final Minecraft p_resize_1_, final int p_resize_2_, final int p_resize_3_)
    {
        this.closeSubPage();
        super.resize(p_resize_1_, p_resize_2_, p_resize_3_);
    }

}
