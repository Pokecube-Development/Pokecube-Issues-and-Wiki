package pokecube.core.client.gui.watch.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.ITextComponent;
import pokecube.core.client.gui.helper.ScrollGui;

public class LineEntry extends AbstractList.AbstractListEntry<LineEntry>
{
    public static interface IClickListener
    {
        default boolean handleClick(final ITextComponent component)
        {
            return false;
        }

        default void handleHovor(final ITextComponent component, final int x, final int y)
        {

        }

    }

    final FontRenderer          fontRender;
    final int                   colour;
    public final ITextComponent line;
    public int                  x0;
    public int                  y0;
    private IClickListener      listener = new IClickListener()
                                         {
                                         };

    @SuppressWarnings("deprecation")
    public LineEntry(final ScrollGui<LineEntry> parent, final int x0, final int y0, final FontRenderer fontRender,
            final ITextComponent line, final int default_colour)
    {
        this.list = parent;
        this.fontRender = fontRender;
        this.line = line;
        this.colour = default_colour;
        this.x0 = x0;
        this.y0 = y0;
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int mouseEvent)
    {
        final boolean inBounds = this.isMouseOver(x, y);
        if (inBounds)
        {
            this.listener.handleClick(this.line);
            return true;
        }
        return false;
    }

    @Override
    public void render(final int slotIndex, final int y, final int x, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
    {
        this.fontRender.drawString(this.line.getString(), x + this.x0, y + this.y0, this.colour);
        final int dx = this.fontRender.getStringWidth(this.line.getString());
        final int relativeX = mouseX - x;
        final int relativeY = mouseY - y;
        if (relativeY <= this.fontRender.FONT_HEIGHT && relativeX >= 0 && relativeX <= dx && relativeY > 0)
            this.listener.handleHovor(this.line, x, y);
    }

    public LineEntry setClickListner(IClickListener listener)
    {
        if (listener == null) listener = new IClickListener()
        {
        };
        this.listener = listener;
        return this;
    }

}
