package pokecube.core.handlers;

import net.minecraft.world.entity.decoration.Motive;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.core.PokecubeCore;

public class PaintingsHandler {

    public static Motive createPainting(String id, int width, int height)
    {
        final Motive painting = new Motive(width, height);
        painting.setRegistryName(PokecubeCore.MODID, id);
        return painting;
    }

    public static void registerPaintings(Register<Motive> event)
    {
        final IForgeRegistry<Motive> paintings = event.getRegistry();

        paintings.register(createPainting("pokecube", 16, 16));
    }
}
