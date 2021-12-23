package pokecube.core.database.spawns;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.database.util.DataHelpers;
import pokecube.core.database.util.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;

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
        public String spawn_preset;
    }

    public static final class MobEntry
    {
        public String key;
        public int min = 2;
        public int max = 4;
        public float rate = 0;
        public int level = -1;
        public String variance;
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
        final Collection<ResourceLocation> resources = PackFinder.getJsonResources(path);
        this.validLoad = !resources.isEmpty();
        MASTER_LIST.rules.clear();
        preLoad();
        resources.forEach(l -> this.loadFile(l));
        if (this.validLoad)
        {
            PokecubeCore.LOGGER.debug("Loaded Pokemob spawns.");
            valid.set(true);
        }
    }

    private void apply()
    {
        PokecubeCore.LOGGER.debug("Applying Pokemob spawns.");
        MASTER_LIST.rules.forEach(entry -> {

            String[] presets = entry.spawn_preset.split(",");
            String preset = presets[0];
            SpawnRule rule = SpawnPresets.PRESETS.get(preset);

            if (presets.length > 1)
            {
                // In this case, we merge all of the other rules in via
                // ANDPRESETS
                rule = new SpawnRule();
                rule.values.put(SpawnBiomeMatcher.ANDPRESET, entry.spawn_preset);
            }

            if (rule != null)
            {
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

                        customRule.values.put(new QName("min"), mob.min + "");
                        customRule.values.put(new QName("max"), mob.max + "");
                        customRule.values.put(new QName("rate"), mob.rate + "");
                        if (mob.level > 0) customRule.values.put(new QName("level"), mob.level + "");
                        if (mob.variance != null) customRule.values.put(new QName("variance"), mob.variance);
                        final SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(customRule);
                        PokedexEntryLoader.handleAddSpawn(poke, matcher);
                    }
                    else
                    {
                        PokecubeCore.LOGGER.error("Error with key {} for spawns", key);
                    }
                });
            }
        });
        PokecubeCore.LOGGER.debug("Applied Pokemob spawns.");
    }

    private void loadFile(final ResourceLocation l)
    {
        try
        {
            final List<SpawnList> loaded = Lists.newArrayList();

            // This one we just take the first resourcelocation. If someone
            // wants to edit an existing one, it means they are most likely
            // trying to remove default behaviour. They can add new things by
            // just adding another json file to the correct package.
            InputStream res = PackFinder.getStream(l);
            final Reader reader = new InputStreamReader(res);
            try
            {
                final SpawnList temp = JsonUtil.gson.fromJson(reader, SpawnList.class);
                if (!confirmNew(temp, l)) return;
                if (temp.replace) loaded.clear();
                loaded.add(temp);
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeCore.LOGGER.error("Malformed Json for Mutations in {}", l);
                PokecubeCore.LOGGER.error(e);
            }
            reader.close();

            for (final SpawnList m : loaded)
            {
                final List<SpawnEntry> conds = m.rules;
                for (final SpawnEntry rule : conds)
                {
                    String preset = rule.spawn_preset;
                    if (preset == null)
                    {
                        PokecubeCore.LOGGER.error("Missing preset tag for {}, skipping it.", rule.entries);
                        continue;
                    }
                    MASTER_LIST.rules.add(rule);
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
