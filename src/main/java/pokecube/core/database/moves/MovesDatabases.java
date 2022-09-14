package pokecube.core.database.moves;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.database.moves.json.JsonMoves.MovesJson;
import pokecube.core.database.resources.PackFinder;

public class MovesDatabases
{
    public static final String DATABASES = "database/moves/";

    public static void preInitLoad()
    {
        final Map<ResourceLocation, Resource> resources = PackFinder.getResources(MovesDatabases.DATABASES,
                s -> s.endsWith(".json") && !s.endsWith("_anims.json"));
        List<MovesJson> loaded = new ArrayList<>();
        resources.forEach((s, r) -> {
            try
            {
                if (s.toString().contains("//")) s = new ResourceLocation(s.toString().replace("//", "/"));
                MovesJson to_load = JsonMoves
                        .merge(new ResourceLocation(s.getNamespace(), s.getPath().replace(".json", "_anims.json")), s);
                if (to_load != null) loaded.add(to_load);
            }
            catch (final Exception e1)
            {
                PokecubeAPI.LOGGER.error("Error with moves database " + s, e1);
            }
        });
        Map<String, MoveJsonEntry> typesMap = new HashMap<>();
        loaded.forEach(l -> l.moves.forEach(t -> typesMap.putIfAbsent(t.name, t)));
        JsonMoves.moves.moves.addAll(typesMap.values());
        JsonMoves.postProcess();
    }
}
