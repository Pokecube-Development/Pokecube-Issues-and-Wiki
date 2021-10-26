package pokecube.core.database.moves;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.resources.PackFinder;

public class MovesDatabases
{
    public static final String DATABASES = "database/moves/";

    public static void preInitLoad()
    {
        final Collection<ResourceLocation> resources = PackFinder.getResources(MovesDatabases.DATABASES, s -> s
                .endsWith(".json") && !s.endsWith("_anims.json"));
        for (ResourceLocation s : resources)
            try
            {
                if (s.toString().contains("//")) s = new ResourceLocation(s.toString().replace("//", "/"));
                JsonMoves.merge(new ResourceLocation(s.getNamespace(), s.getPath().replace(".json", "_anims.json")), s);
            }
            catch (final Exception e1)
            {
                PokecubeCore.LOGGER.error("Error with moves database " + s, e1);
            }
    }
}
