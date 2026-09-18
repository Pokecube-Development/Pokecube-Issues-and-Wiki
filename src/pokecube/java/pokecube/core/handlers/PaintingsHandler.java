package pokecube.core.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;
import pokecube.core.PokecubeCore;

public class PaintingsHandler
{

    public static PaintingVariant createPainting(String id, int width, int height)
    {
        return new PaintingVariant(width, height,
                ResourceLocation.fromNamespaceAndPath("pokecube", id));
    }

    public static void init()
    {
        PokecubeCore.PAINTINGS.register("pokecube", () -> createPainting("pokecube", 16, 16));
    }
}
