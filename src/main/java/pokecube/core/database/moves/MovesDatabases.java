package pokecube.core.database.moves;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.moves.json.JsonMoves;

public class MovesDatabases
{
    public static final String DATABASES = "database/moves/";

    public static void preInitLoad()
    {
        final Collection<ResourceLocation> resources = Database.resourceManager.getAllResourceLocations(
                MovesDatabases.DATABASES, s -> s.endsWith(".json") && !s.endsWith("_anims.json"));
        for (final ResourceLocation s : resources)
            try
            {
                JsonMoves.merge(new ResourceLocation(s.getNamespace(), s.getPath().replace(".json", "_anims.json")), s);
            }
            catch (final Exception e1)
            {
                PokecubeCore.LOGGER.error("Error with moves database " + s, e1);
            }
    }
}
