package pokecube.core.client.gui.watch.util;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

    final Font          fontRender;
    final int                   colour;
    public final Component line;
    public int                  x0;
    public int                  y0;
    private IClickListener      listener = new IClickListener()
                                         {
                                         };

    @SuppressWarnings("deprecation")
    public LineEntry(final ScrollGui<LineEntry> parent, final int x0, final int y0, final Font fontRender,
            final Component line, final int default_colour)
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
            if (this.listener.handleClick(this.line.getStyle())) return true;
            for (final Component comp : this.line.getSiblings())
                if (this.listener.handleClick(comp.getStyle())) return true;
            return true;
        }
        return false;
    }

    @Override
    public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
            final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
            final float partialTicks)
    {
        this.fontRender.draw(mat, this.line.getString(), x + this.x0, y + this.y0, this.colour);
        final int dx = this.fontRender.width(this.line.getString());
        final int relativeX = mouseX - x;
        final int relativeY = mouseY - y;
        if (relativeY <= this.fontRender.lineHeight && relativeX >= 0 && relativeX <= dx && relativeY > 0)
            this.listener.handleHovor(mat, this.line.getStyle(), x, y);
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
