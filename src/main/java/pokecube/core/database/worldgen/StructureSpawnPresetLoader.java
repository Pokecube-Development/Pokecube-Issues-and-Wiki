package pokecube.core.database.worldgen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.entity.npc.NpcType;

public class StructureSpawnPresetLoader
{
    public static Map<String, JsonObject> presetMap = Maps.newHashMap();

    public static class SpawnPresets
    {
        List<JsonObject> presets = Lists.newArrayList();
    }

    public static void loadDatabase()
    {
        final Collection<ResourceLocation> resources = Database.resourceManager.listResources(
                NpcType.DATALOC, s -> s.endsWith(".json"));
        for (final ResourceLocation file : resources)
        {
            JsonObject loaded;
            try
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(Database.resourceManager
                        .getResource(file).getInputStream()));
                loaded = PokedexEntryLoader.gson.fromJson(reader, JsonObject.class);
                reader.close();
                if (loaded.has("presets"))
                {
                    final SpawnPresets database = PokedexEntryLoader.gson.fromJson(loaded, SpawnPresets.class);
                    for (final JsonObject preset : database.presets)
                        if (preset.has("preset_name")) StructureSpawnPresetLoader.presetMap.put(preset.get(
                                "preset_name").getAsString(), preset);
                        else PokecubeCore.LOGGER.error("Warning, needs a \"preset_name\" field for " + preset);
                }

            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error loading npc presets from {}", file, e);
            }
        }
    }
}
