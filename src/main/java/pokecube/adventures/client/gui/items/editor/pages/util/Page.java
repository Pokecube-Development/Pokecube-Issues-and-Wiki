package pokecube.adventures.client.gui.items.editor.pages.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.client.gui.items.editor.EditorGui;

public abstract class Page extends Screen implements IGuiEventListener
{
    public final EditorGui       parent;
    private final ITextComponent title;

    public Page(final ITextComponent title, final EditorGui parent)
    {
        super(title);
        this.title = title;
        this.parent = parent;
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
        this.parent.children().remove(this);
    }

    public void onPageOpened()
    {
        @SuppressWarnings("unchecked")
        final List<IGuiEventListener> list = (List<IGuiEventListener>) this.parent.children();
        list.add(this);
    }
}
