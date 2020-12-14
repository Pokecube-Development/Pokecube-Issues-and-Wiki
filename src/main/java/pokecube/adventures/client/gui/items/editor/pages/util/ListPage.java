package pokecube.adventures.client.gui.items.editor.pages.util;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.ITextComponent;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.core.client.gui.helper.ScrollGui;

public abstract class ListPage<T extends AbstractList.AbstractListEntry<T>> extends Page
{

    protected ScrollGui<T> list;
    /**
     * Set this to true if the page handles rendering the list itself.
     */
    protected boolean      handlesList = false;

    public ListPage(final ITextComponent title, final EditorGui parent)
    {
        super(title, parent);
    }

    public void drawTitle(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.parent.width - 160) / 2 + 80;
        final int y = (this.parent.height - 160) / 2 + 8;
        AbstractGui.drawCenteredString(mat, this.font, this.getTitle().getString(), x, y, 0xFFFFFFFF);
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
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawTitle(mat, mouseX, mouseY, partialTicks);
        super.render(mat, mouseX, mouseY, partialTicks);
        // Draw the list
        if (!this.handlesList) this.list.render(mat, mouseX, mouseY, partialTicks);
    }
}
