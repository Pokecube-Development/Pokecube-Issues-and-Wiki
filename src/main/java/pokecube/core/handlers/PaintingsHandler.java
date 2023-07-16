package pokecube.core.handlers;

import net.minecraft.world.entity.decoration.Motive;
import pokecube.core.PokecubeCore;

public class PaintingsHandler
{
    public static Motive createPainting(String id, int width, int height)
    {
        final Motive painting = new Motive(width, height);
        return painting;
    }

    public static void init()
    {
        PokecubeCore.PAINTINGS.register("pokecube", () -> createPainting("pokecube", 16, 16));
    }
}
