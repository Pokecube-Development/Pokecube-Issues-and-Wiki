package thut.wearables.client.render.slots;

import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3f;

public class Utils
{
    public static void mirror(float x, float y, float z, final GuiGraphics graphics)
    {
        if (x == 0) x = 1;
        else x = -1;
        if (y == 0) y = 1;
        else y = -1;
        if (z == 0) z = 1;
        else z = -1;
        // TODO: Check this
        final Matrix3f norms = graphics.pose().last().normal()/*.copy()*/;
        graphics.pose().scale(x, y, z);
        graphics.pose().last().normal().add(norms);

    }
}
