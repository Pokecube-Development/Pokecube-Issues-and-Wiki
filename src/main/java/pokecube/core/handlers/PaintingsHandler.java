package pokecube.core.handlers;

import net.minecraft.entity.item.PaintingType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;

public class PaintingsHandler {

    public static PaintingType createPainting(String id, int width, int height)
    {
        final PaintingType painting = new PaintingType(width, height);
        painting.setRegistryName(PokecubeCore.MODID, id);
        return painting;
    }

    public static void registerPaintings(Register<PaintingType> event)
    {
        final IForgeRegistry<PaintingType> paintings = event.getRegistry();

        paintings.register(createPainting("pokecube", 16, 16));
    }
}
