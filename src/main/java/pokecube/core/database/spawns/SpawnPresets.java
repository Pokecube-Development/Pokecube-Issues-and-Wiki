package pokecube.core.database.spawns;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

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
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        this.validLoad = !resources.isEmpty();
        PRESETS.clear();
        preLoad();
        resources.forEach((l, r) -> this.loadFile(l, r));
        if (this.validLoad)
        {
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded Spawn Rule presets.");
            valid.set(true);
        }
    }

    private void loadFile(final ResourceLocation l, Resource r)
    {
        try
        {
            final List<MatcherList> loaded = Lists.newArrayList();

            // This one we just take the first resourcelocation. If someone
            // wants to edit an existing one, it means they are most likely
            // trying to remove default behaviour. They can add new things by
            // just adding another json file to the correct package.
            final BufferedReader reader = ResourceHelper.getReader(r);
            if (reader == null) throw new FileNotFoundException(l.toString());
            try
            {
                final MatcherList temp = JsonUtil.gson.fromJson(reader, MatcherList.class);
                if (temp.replace) loaded.clear();
                if (!confirmNew(temp, l))
                {
                    reader.close();
                    return;
                }
                loaded.add(temp);
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeAPI.LOGGER.error("Malformed Json for Mutations in {}", l);
                PokecubeAPI.LOGGER.error(e);
            }
            reader.close();

            for (final MatcherList m : loaded)
            {
                final List<SpawnRule> conds = m.rules;
                for (final SpawnRule rule : conds)
                {
                    String preset = rule.preset.isBlank() ? null : rule.preset;
                    if (preset == null)
                    {
                        PokecubeAPI.LOGGER.error("Missing preset tag for {}, skipping it.", rule.values);
                        continue;
                    }
                    PRESETS.put(preset, rule);
                }
            }
        }
        catch (final Exception e)
        {
            // Might not be valid, so log and skip in that case.
            PokecubeAPI.LOGGER.error("Error with resources in {}", l);
            PokecubeAPI.LOGGER.error(e);
        }
    }
}
