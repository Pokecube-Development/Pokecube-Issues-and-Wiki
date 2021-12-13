package pokecube.core.database.spawns;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.util.DataHelpers.ResourceData;

public class SpawnPresets extends ResourceData
{
    public static final SpawnPresets INSTANCE = new SpawnPresets("database/spawn_rule_presets/");

    public static void init()
    {}

    public static final class MatcherList
    {
        public List<SpawnRule> rules = Lists.newArrayList();
        public boolean replace = false;
    }

    public static final Map<String, SpawnRule> PRESETS = SpawnBiomeMatcher.PRESETS;

    private final String tagPath;

    public boolean validLoad = false;

    public SpawnPresets(final String string)
    {
        super(string);
        this.tagPath = string;
        DataHelpers.addDataType(this);
    }

    @Override
    public void reload(AtomicBoolean valid)
    {
        this.validLoad = false;
        final String path = new ResourceLocation(this.tagPath).getPath();
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources(path);
        this.validLoad = !resources.isEmpty();
        PRESETS.clear();
        preLoad();
        resources.forEach(l -> this.loadFile(l));
        if (this.validLoad)
        {
            PokecubeCore.LOGGER.debug("Loaded Spawn Rule presets.");
            valid.set(true);
        }
    }

    private void loadFile(final ResourceLocation l)
    {
        try
        {
            final List<MatcherList> loaded = Lists.newArrayList();

            // This one we just take the first resourcelocation. If someone
            // wants to edit an existing one, it means they are most likely
            // trying to remove default behaviour. They can add new things by
            // just adding another json file to the correct package.
            InputStream res = PackFinder.getStream(l);
            final Reader reader = new InputStreamReader(res);
            try
            {
                final MatcherList temp = PokedexEntryLoader.gson.fromJson(reader, MatcherList.class);
                if (temp.replace) loaded.clear();
                if (!confirmNew(temp, l)) return;
                loaded.add(temp);
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeCore.LOGGER.error("Malformed Json for Mutations in {}", l);
                PokecubeCore.LOGGER.error(e);
            }
            reader.close();

            for (final MatcherList m : loaded)
            {
                final List<SpawnRule> conds = m.rules;
                for (final SpawnRule rule : conds)
                {
                    String preset = rule.values.get(SpawnBiomeMatcher.PRESET);
                    if (preset == null)
                    {
                        PokecubeCore.LOGGER.error("Missing preset tag for {}, skipping it.", rule.values);
                        continue;
                    }
                    PRESETS.put(preset, rule);
                }
            }
        }
        catch (final Exception e)
        {
            // Might not be valid, so log and skip in that case.
            PokecubeCore.LOGGER.error("Error with resources in {}", l);
            PokecubeCore.LOGGER.error(e);
        }
    }
}
