package pokecube.core.client.gui.helper;

public class Rectangle
{
    public final int x0, x1, y0, y1;
    public final int h, w;

    public Rectangle(int x0, int y0, int x1, int y1)
    {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.w = Math.abs(x0 - x1);
        this.h = Math.abs(y0 - y1);
    }

    public boolean isInside(double mx, double my)
    {
        return my < y1 && my > y0 && mx < x1 && mx > x0;
    }
}
