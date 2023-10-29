package pokecube.core.client.gui.helper;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;

public class Rectangle
{
    public int x0, x1, y0, y1;
    public int h, w;

    public Rectangle()
    {
        this(0, 0, 0, 0);
    }

    public Rectangle(int x0, int y0, int x1, int y1)
    {
        this.setCorners(x0, y0, x1, y1);
    }

    public void setBox(Rectangle bounds)
    {
        this.setBox(bounds.x0, bounds.y0, bounds.w, bounds.h);
    }

    public void setCorners(int x0, int y0, int x1, int y1)
    {
        this.x0 = Math.min(x0, x1);
        this.x1 = Math.max(x0, x1);
        this.y0 = Math.min(y0, y1);
        this.y1 = Math.max(y0, y1);
        this.w = Math.abs(x0 - x1);
        this.h = Math.abs(y0 - y1);
    }

    public void setBox(int x0, int y0, int w, int h)
    {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x0 + w;
        this.y1 = y0 + h;
        this.w = w;
        this.h = h;
    }

    public void clipToWindow()
    {
        Minecraft minecraft = Minecraft.getInstance();
        Window res = minecraft.getWindow();
        int scaledWidth = res.getGuiScaledWidth();
        int scaledHeight = res.getGuiScaledHeight();
        int dx = this.x0;
        int dy = this.y0;
        if (dx < 0) this.setBox(x0 = (x0 - dx), y0, w, h);
        if (dy < 0) this.setBox(x0, y0 = (y0 - dy), w, h);
        dx = scaledWidth - x1;
        dy = scaledHeight - y1;
        if (dx < 0) this.setBox(x0 = (x0 + dx), y0, w, h);
        if (dy < 0) this.setBox(x0, y0 = (y0 + dy), w, h);
    }

    public boolean isInside(double mx, double my)
    {
        return my < y1 && my > y0 && mx < x1 && mx > x0;
    }
}
