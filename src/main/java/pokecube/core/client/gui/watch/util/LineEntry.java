package pokecube.core.client.gui.watch.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import pokecube.core.client.gui.helper.ScrollGui;

public class LineEntry extends AbstractSelectionList.Entry<LineEntry>
{
    public static interface IClickListener
    {
        default boolean handleClick(final Style component)
        {
            return false;
        }

        default void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
        {

        }

    }

    final Font font;
    final int colour;
    public final FormattedCharSequence line;
    public int x0;
    public int y0;
    private boolean shadowed = false;

    private int x1 = 0;
    private IClickListener listener = new IClickListener()
    {
    };

    @SuppressWarnings("deprecation")
    public LineEntry(final ScrollGui<LineEntry> parent, final int x0, final int y0, final Font fontRender,
            final FormattedCharSequence line, final int default_colour)
    {
        this.list = parent;
        this.font = fontRender;
        this.line = line;
        this.colour = default_colour;
        this.x0 = x0;
        this.y0 = y0;
    }

    public LineEntry shadow()
    {
        shadowed = true;
        return this;
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int mouseEvent)
    {
        final boolean inBounds = this.isMouseOver(x, y);
        if (inBounds)
        {
            if (this.listener.handleClick(this.getStyle(x))) return true;
        }
        return false;
    }

    @Override
    public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
            final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
            final float partialTicks)
    {
        if (shadowed) this.font.drawShadow(mat, this.line, x + this.x0, y + this.y0, this.colour);
        else this.font.draw(mat, this.line, x + this.x0, y + this.y0, this.colour);
        x1 = x;
        final int dx = this.font.width(this.line);
        final int relativeX = mouseX - x;
        final int relativeY = mouseY - y;
        if (relativeY <= this.font.lineHeight && relativeX >= 0 && relativeX <= dx && relativeY > 0)
            this.listener.handleHovor(mat, this.getStyle(mouseX), x, y);
    }

    private Style getStyle(double x)
    {
        int dx = (int) (x - x0 - x1);
        if (dx < 0) return Style.EMPTY;
        Style style = this.font.getSplitter().componentStyleAtWidth(line, dx);
        if (style == null) style = Style.EMPTY;
        return style;
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
