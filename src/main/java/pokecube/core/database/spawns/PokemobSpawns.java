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
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.DefaultFormeHolder;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.resources.PackFinder;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class PokemobSpawns extends ResourceData
{
    public static final PokemobSpawns INSTANCE = new PokemobSpawns("database/pokemobs/spawns/");

    public static void init()
    {}

    public static final class SpawnList
    {
        public List<SpawnEntry> rules = Lists.newArrayList();
        public boolean replace = false;
    }

    public static final class SpawnEntry
    {
        public List<MobEntry> entries = Lists.newArrayList();
        public String and_preset;
        public String not_preset;
        public String or_preset;
    }

    public static final class MobEntry
    {
        public String key;
        public int min = 2;
        public int max = 4;
        public float rate = 0;
        public int level = -1;
        public String variance;
        public String variant = "";
    }

    private static final SpawnList MASTER_LIST = new SpawnList();

    public static void registerSpawns()
    {
        INSTANCE.apply();
    }

    private final String tagPath;

    public boolean validLoad = false;

    public PokemobSpawns(final String string)
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
        MASTER_LIST.rules.clear();
        preLoad();
        resources.forEach((l, r) -> this.loadFile(l, r));
        if (this.validLoad)
        {
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded Pokemob spawns.");
            valid.set(true);
        }
    }

    private void apply()
    {
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Applying Pokemob spawns.");
        MASTER_LIST.rules.forEach(entry -> {

            SpawnRule rule = null;

            if (entry.and_preset != null)
            {
                String[] presets = entry.and_preset.split(",");
                String preset = presets[0];
                rule = SpawnPresets.PRESETS.get(preset);
                if (rule != null) rule = rule.copy();

                if (presets.length > 1)
                {
                    // In this case, we merge all of the other rules in via
                    // ANDPRESETS
                    rule = new SpawnRule();
                    rule.values.put(SpawnBiomeMatcher.ANDPRESET, entry.and_preset);
                }
            }

            if (entry.or_preset != null)
            {
                if (rule == null)
                {
                    String[] presets = entry.or_preset.split(",");
                    String preset = presets[0];
                    rule = SpawnPresets.PRESETS.get(preset);
                    if (rule != null) rule = rule.copy();

                    if (presets.length > 1)
                    {
                        // In this case, we merge all of the other rules in via
                        // ORPRESETS
                        rule = new SpawnRule();
                        rule.values.put(SpawnBiomeMatcher.ORPRESET, entry.or_preset);
                    }
                }
                else
                {
                    rule.values.put(SpawnBiomeMatcher.ORPRESET, entry.or_preset);
                }
            }

            if (rule != null)
            {

                if (entry.not_preset != null)
                {
                    // Finally add in the NOTPRESET
                    rule.values.put(SpawnBiomeMatcher.NOTPRESET, entry.not_preset);
                }

                // Final instance of rule so that it works in the below lambda
                SpawnRule frule = rule;

                entry.entries.forEach(mob -> {
                    String key = mob.key;

                    PokedexEntry poke = Database.getEntry(key);
                    // For now, we will just support direct entries, later will
                    // will fix this to lookup in the Database formeToEntry, and
                    // then also define models there.
                    if (poke != null)
                    {
                        SpawnRule customRule = frule.copy();

                        if (!mob.variant.isBlank())
                        {
                            customRule.model = new DefaultFormeHolder();
                            customRule.model.key = mob.variant;
                        }

                        customRule.values.put("min", mob.min + "");
                        customRule.values.put("max", mob.max + "");
                        customRule.values.put("rate", mob.rate + "");
                        if (mob.level > 0) customRule.values.put("level", mob.level + "");
                        if (mob.variance != null) customRule.values.put("variance", mob.variance);
                        final SpawnBiomeMatcher matcher = SpawnBiomeMatcher.get(customRule);
                        PokedexEntryLoader.handleAddSpawn(poke, matcher);
                    }
                    else
                    {
                        PokecubeAPI.LOGGER.error("Error with key {} for spawns", key);
                    }
                });
            }
        });
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Applied Pokemob spawns.");
    }

    private void loadFile(final ResourceLocation l, Resource r)
    {
        try
        {
            final List<SpawnList> loaded = Lists.newArrayList();

            // This one we just take the first resourcelocation. If someone
            // wants to edit an existing one, it means they are most likely
            // trying to remove default behaviour. They can add new things by
            // just adding another json file to the correct package.
            final BufferedReader reader = ResourceHelper.getReader(r);
            if (reader == null) throw new FileNotFoundException(l.toString());
            try
            {
                final SpawnList temp = JsonUtil.gson.fromJson(reader, SpawnList.class);
                if (!confirmNew(temp, l))
                {
                    reader.close();
                    return;
                }
                if (temp.replace) loaded.clear();
                loaded.add(temp);
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeAPI.LOGGER.error("Malformed Json for Mutations in {}", l);
                PokecubeAPI.LOGGER.error(e);
            }
            reader.close();

            for (final SpawnList m : loaded)
            {
                final List<SpawnEntry> conds = m.rules;
                for (final SpawnEntry rule : conds)
                {
                    if (rule.and_preset == null && rule.or_preset == null)
                    {
                        PokecubeAPI.LOGGER.error("Missing preset tag for {}, skipping it.", rule.entries);
                        continue;
                    }
                    MASTER_LIST.rules.add(rule);
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
