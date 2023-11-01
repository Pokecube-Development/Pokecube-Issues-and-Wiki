package pokecube.core.client.gui.components;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.helper.Rectangle;

public abstract class GuiEventComponent implements Comparable<GuiEventComponent>
{
    public static void applyTransform(GuiEventComponent component)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final Window res = minecraft.getWindow();
        int x0 = component.bounds.x0;
        int y0 = component.bounds.y0;
        int w = component.bounds.w;
        int h = component.bounds.h;
        final int sW = res.getGuiScaledWidth();
        final int sH = res.getGuiScaledHeight();
        switch (component.ref)
        {
        case "top_left":
            // Top left reference, just set to bounds
            break;
        case "middle_left":
            y0 += sH / 2 - h;
            break;
        case "bottom_left":
            y0 += sH - h;
            break;
        case "top_right":
            x0 += sW - w;
            break;
        case "right_bottom":
            x0 += sW - w;
            y0 += sH - h;
            break;
        case "right_middle":
            x0 += sW - w;
            y0 += sH / 2 - h;
            break;
        case "bottom_middle":
            x0 += sW / 2 - w;
            y0 += sH / 2 - h;
            break;
        }
        component.pos.setBox(x0, y0, w, h);
        component.pos.clipToWindow();
    }

    int zLevel = 0;
    float scale = 1.0f;

    public String ref = "top_left";

    public int clickA = 0;
    public int clickB = 0;
    public int clickM = 0;
    int mX;
    int mY;

    protected final Rectangle bounds = new Rectangle();
    protected final Rectangle pos = new Rectangle();

    public int getZLevel()
    {
        return this.zLevel;
    }

    public void drawBounds(GuiEvent event)
    {
        GuiComponent.fill(event.getMat(), pos.x0, pos.y0, pos.x1, pos.y1, 0x44FF0000);
    }

    protected void preDraw(GuiEvent event)
    {}

    public void drawGui(GuiEvent event)
    {
        this.preDraw(event);
        if (Screen.hasAltDown() && GuiInfoMessages.fullDisplay()) drawBounds(event);
        applyTransform(this);
        this._drawGui(event);
    }

    public abstract void _drawGui(GuiEvent event);

    public boolean handleClick(int action, int mouseButton, int modifiers)
    {
        boolean over = isMouseOver();
        // If not over, say not clicked, and not dragged
        if (action == 1 && mouseButton == 0 && !over)
        {
            clickA = 0;
            clickB = 0;
            clickM = 0;
            return false;
        }
        if (action == 0 && clickA == 1) this.onMovedGui();

        clickA = action;
        clickB = mouseButton;
        double x = Minecraft.getInstance().mouseHandler.xpos();
        double y = Minecraft.getInstance().mouseHandler.ypos();

        Minecraft minecraft = Minecraft.getInstance();
        Window res = minecraft.getWindow();
        float scaledWidth = res.getGuiScaledWidth();
        float scaledHeight = res.getGuiScaledHeight();
        float width = res.getWidth();
        float height = res.getHeight();

        float sx = width / scaledWidth;
        float sy = height / scaledHeight;
        x /= sx;
        y /= sy;

        // Mark the relative screen coordinates for where we clicked, for
        // detecting dragging.
        mX = (int) x;
        mY = (int) y;
        return over && clickA == 1;
    }

    protected void onMovedGui()
    {
        GuiDisplayPokecubeInfo.instance().saveConfig();
    }

    public boolean isMouseOver()
    {
        double x = Minecraft.getInstance().mouseHandler.xpos();
        double y = Minecraft.getInstance().mouseHandler.ypos();

        Minecraft minecraft = Minecraft.getInstance();
        Window res = minecraft.getWindow();
        float scaledWidth = res.getGuiScaledWidth();
        float scaledHeight = res.getGuiScaledHeight();
        float width = res.getWidth();
        float height = res.getHeight();

        float sx = width / scaledWidth;
        float sy = height / scaledHeight;
        x /= sx;
        y /= sy;

        if (clickA == 1 && clickB == 0 && GuiInfoMessages.fullDisplay())
        {
            int dx = (int) (x - mX);
            int dy = (int) (y - mY);
            if (dx != 0 || dy != 0)
            {
                // Apply where we are, to get an initial clipped location
                applyTransform(this);
                int x0 = pos.x0;
                int y0 = pos.y0;
                bounds.setBox(bounds.x0 + dx, bounds.y0 + dy, bounds.w, bounds.h);
                // Now apply after moving
                applyTransform(this);
                // If these are not zero, it means we clipped
                int dx2 = pos.x0 - x0 - dx;
                int dy2 = pos.y0 - y0 - dy;
                // so also clip bounds in this case, by the same amound.
                bounds.setBox(bounds.x0 + dx2, bounds.y0 + dy2, bounds.w, bounds.h);
                // Now reset the marker for mouse has moved.
                mX = (int) x;
                mY = (int) y;
            }
        }
        return pos.isInside(x, y);
    }

    @Override
    public int compareTo(GuiEventComponent o)
    {
        return Integer.compare(zLevel, o.zLevel);
    }
}
