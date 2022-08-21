package pokecube.core.database.moves;

import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.resources.PackFinder;

public class MovesDatabases
{
    public static final String DATABASES = "database/moves/";

    public static void preInitLoad()
    {
        final Map<ResourceLocation, Resource> resources = PackFinder.getResources(MovesDatabases.DATABASES,
                s -> s.endsWith(".json") && !s.endsWith("_anims.json"));
        resources.forEach((s, r) -> {
            try
            {
                if (s.toString().contains("//")) s = new ResourceLocation(s.toString().replace("//", "/"));
                JsonMoves.merge(new ResourceLocation(s.getNamespace(), s.getPath().replace(".json", "_anims.json")), s);
            }
            catch (final Exception e1)
            {
                PokecubeAPI.LOGGER.error("Error with moves database " + s, e1);
            }
        });
    }
}
