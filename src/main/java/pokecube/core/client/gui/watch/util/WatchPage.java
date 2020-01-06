package pokecube.core.client.gui.watch.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.client.gui.watch.GuiPokeWatch;

public abstract class WatchPage extends Screen implements IGuiEventListener
{
    public final GuiPokeWatch    watch;
    private final ITextComponent title;

    public WatchPage(final ITextComponent title, final GuiPokeWatch watch)
    {
        super(title);
        this.title = title;
        this.watch = watch;
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.fontRenderer;
    }

    @Override
    public ITextComponent getTitle()
    {
        return this.title;
    }

    @Override
    /**
     * This is made public here to make it accessible for others.
     */
    public void init()
    {
        this.onPageClosed();
        super.init();
    }

    public void onPageClosed()
    {
        this.watch.children().remove(this);
    }

    public void onPageOpened()
    {
        @SuppressWarnings("unchecked")
        final List<IGuiEventListener> list = (List<IGuiEventListener>) this.watch.children();
        list.add(this);
    }
}
