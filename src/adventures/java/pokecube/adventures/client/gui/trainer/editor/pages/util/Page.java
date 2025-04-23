package pokecube.adventures.client.gui.trainer.editor.pages.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;

public abstract class Page extends Screen implements GuiEventListener
{
    public final EditorGui parent;
    private final Component title;

    // this can be easily called by buttons to go back to previous page.
    public Runnable closeCallback = () -> {};

    public Page(final Component title, final EditorGui parent)
    {
        super(title);
        this.title = title;
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.font = this.minecraft.font;
    }

    @Override
    public Component getTitle()
    {
        return this.title;
    }

    /**
     * @return Whether this page is valid for this parent.
     */
    public boolean isValid()
    {
        return true;
    }

    /**
     * This is made public here to make it accessible for others.
     */
    @Override
    public void init()
    {
        this.onPageClosed();
        super.init();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        // We overwrite this to reverse the ordering of checking if tab was
        // pressed
        return this.getFocused() != null && this.getFocused().keyPressed(keyCode, b, c);
    }

    public void onPageClosed()
    {
        this.parent.children().remove(this);
        this.clearWidgets();
    }

    public void onPageOpened()
    {
        this.clearWidgets();
        this.renderables.add(this::renderPage);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
    }

    public void renderPage(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
    }
}
