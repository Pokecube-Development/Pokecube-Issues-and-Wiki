package pokecube.api.data.effects;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader.IMergeable;
import pokecube.core.database.resources.PackFinder;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class PokemobEffects implements IMergeable<PokemobEffects>
{
    public boolean replace = false;

    // Material affects, such as harm in water, despawning in light, etc
    public List<JsonObject> material_rules = Lists.newArrayList();

    // list of pokemobs to apply this to.
    public List<String> pokemobs = Lists.newArrayList();

    @Override
    public PokemobEffects mergeFrom(PokemobEffects other)
    {
        if (other.replace) return other;
        // TODO let this merge instead of replacing.
        return other;
    }

    private void init()
    {
        var effects = MaterialEffects.fromJson(material_rules);
        pokemobs.forEach(name -> {
            PokedexEntry entry = Database.getEntry(name);
            if (entry != null) entry.materialActions = effects;
            else PokecubeAPI.LOGGER.error("No pokemob by name {} for loading material effects", name);
        });
    }

    public static void loadMaterials()
    {
        final String path = "database/pokemobs/materials/";
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        Map<String, List<PokemobEffects>> toLoad = Maps.newHashMap();
        resources.forEach((l, r) -> {
            try
            {
                final PokemobEffects entry = loadDatabase(ResourceHelper.getStream(r));
                toLoad.compute(l.getPath(), (key, list) -> {
                    var ret = list;
                    if (ret == null) ret = Lists.newArrayList();
                    ret.add(entry);
                    return ret;
                });
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error with pokemob materials file {}", l, e);
            }
        });

        List<PokemobEffects> loaded = Lists.newArrayList();
        loaded.clear();

        toLoad.forEach((k, v) -> {
            v.sort(null);
            PokemobEffects e = null;
            for (PokemobEffects e1 : v)
            {
                if (e == null) e = e1;
                else
                {
                    e = e.mergeFrom(e1);
                }
            }
            loaded.add(e);
        });

        loaded.forEach(effects -> {
            effects.init();
        });
    }

    private static PokemobEffects loadDatabase(final InputStream stream) throws Exception
    {
        PokemobEffects database = null;
        final InputStreamReader reader = new InputStreamReader(stream);
        database = JsonUtil.gson.fromJson(reader, PokemobEffects.class);
        reader.close();
        return database;
    }
}
