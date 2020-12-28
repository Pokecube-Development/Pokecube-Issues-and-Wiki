package pokecube.adventures.client.gui.trainer.editor.pages.util;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;

public class PageEntry extends AbstractList.AbstractListEntry<PageEntry>
{
    public final Button button;
    final int           top;

    public PageEntry(final Page parent, final Page page, final int index, final int offsetX, final int offsetY)
    {
        this.top = offsetY;
        this.button = new Button(offsetX, offsetY, 130, 20, page.getTitle(), b -> parent.parent
                .changePage(index));
        this.button.visible = false;
        this.button.active = false;
        parent.addButton(this.button);
    }

    @Override
    public void render(final MatrixStack mat, final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
    {
        this.button.visible = false;
        this.button.active = false;
        // Note that x and y are reversed for this method...
        if (x > this.top && x < this.top + 90)
        {
            this.button.x = y;
            this.button.y = x;
            this.button.visible = true;
            this.button.active = true;

        }
        else
        {
            this.button.visible = false;
            this.button.active = false;
        }
    }
}