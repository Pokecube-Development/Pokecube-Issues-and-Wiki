package pokecube.legends.conditions.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.core.database.resources.PackFinder;
import pokecube.legends.Reference;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.data.Conditions.AndPreset;
import pokecube.legends.conditions.data.Conditions.BuiltCondition;
import pokecube.legends.conditions.data.Conditions.EntriedCondition;
import pokecube.legends.conditions.data.Conditions.OrPreset;
import pokecube.legends.conditions.data.Conditions.PresetCondition;
import pokecube.legends.conditions.data.Conditions.TypedCondition;
import pokecube.legends.spawns.LegendarySpawn;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = Reference.ID)
public class ConditionLoader extends ResourceData
{

    public static Map<String, Class<? extends PresetCondition>> __presets__ = Maps.newHashMap();

    static
    {
        ConditionLoader.__presets__.put("entry_based", EntriedCondition.class);
        ConditionLoader.__presets__.put("type_based", TypedCondition.class);
        ConditionLoader.__presets__.put("spawns_only", PresetCondition.class);
        ConditionLoader.__presets__.put("build", BuiltCondition.class);
        ConditionLoader.__presets__.put("and", AndPreset.class);
        ConditionLoader.__presets__.put("or", OrPreset.class);
    }

    private static boolean POST_LOAD = false;

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event)
    {
        POST_LOAD = true;
    }

    private final String tagPath;

    public boolean validLoad = false;

    public ConditionLoader(final String string)
    {
        super(string);
        this.tagPath = string;
        DataHelpers.addDataType(this);
    }

    List<PresetCondition> conditions = Lists.newArrayList();
    List<AbstractCondition> _added = new ArrayList<>();

    @Override
    public void reload(final AtomicBoolean valid)
    {
        this.validLoad = false;
        if (!POST_LOAD) return;
        final String path = ResourceLocation.parse(this.tagPath).getPath();
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        _added.forEach(e -> {
            ISpecialCaptureCondition.captureMap.remove(e.getEntry());
            ISpecialSpawnCondition.spawnMap.remove(e.getEntry());
        });
        this.validLoad = !resources.isEmpty();
        this.conditions.clear();
        LegendarySpawn.data_spawns.clear();
        this.preLoad();
        resources.forEach(this::loadFile);
        if (this.validLoad) valid.set(true);
    }

    @Override
    public void postReload()
    {
        LegendarySpawn.data_spawns.clear();
        this.conditions.forEach(e -> _added.add(e.register()));
        _added.removeIf(Objects::isNull);
        this.conditions.clear();
    }

    public static PresetCondition fromJson(JsonObject json)
    {
        final String preset = json.get("preset").getAsString();
        final Class<? extends PresetCondition> preset_class = ConditionLoader.__presets__.get(preset);
        if (preset_class == null)
        {
            PokecubeAPI.LOGGER.error("No preset found for {}", preset);
            return null;
        }
        return JsonUtil.gson.fromJson(json, preset_class);
    }

    private void loadFile(final ResourceLocation l, Resource r)
    {
        try
        {
            // This one we just take the first resourcelocation. If someone
            // wants to edit an existing one, it means they are most likely
            // trying to remove default behaviour. They can add new things by
            // just adding another json file to the correct package.
            final BufferedReader reader = ResourceHelper.getReader(r);
            if (reader == null) throw new FileNotFoundException(l.toString());
            try
            {
                var temp = JsonUtil.gson.fromJson(reader, JsonObject.class);
                if (!confirmNew(temp, l))
                {
                    reader.close();
                    return;
                }
                if (!temp.has("preset"))
                {
                    PokecubeAPI.LOGGER.error("No preset in {}", temp);
                    reader.close();
                    return;
                }

                try
                {
                    final PresetCondition actual = fromJson(temp);
                    if (actual == null)
                    {
                        reader.close();
                        return;
                    }
                    this.conditions.add(actual);
                }
                catch (final Exception e)
                {
                    // Might not be valid, so log and skip in that case.
                    PokecubeAPI.LOGGER.error("Error processing a preset in {}", l);
                    PokecubeAPI.LOGGER.error(e);
                }
            }
            catch (final Exception e)
            {
                // Might not be valid, so log and skip in that case.
                PokecubeAPI.LOGGER.error("Malformed Json for Mutations in {}", l);
                PokecubeAPI.LOGGER.error(e);
            }
            reader.close();
        }
        catch (final Exception e)
        {
            // Might not be valid, so log and skip in that case.
            PokecubeAPI.LOGGER.error("Error with resources in {}", l);
            PokecubeAPI.LOGGER.error(e);
        }

    }
}
