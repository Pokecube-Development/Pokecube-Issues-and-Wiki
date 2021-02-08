package pokecube.core.database.worldgen;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;

public class StructureSpawnPresetLoader
{
    public static Map<String, JsonObject> presetMap = Maps.newHashMap();

    public static class SpawnPresets
    {
        List<JsonObject> presets = Lists.newArrayList();
    }

    public static void loadDatabase(final ResourceLocation file) throws Exception
    {
        final InputStream res = Database.resourceManager.getResource(file).getInputStream();
        final Reader reader = new InputStreamReader(res);
        final SpawnPresets database = PokedexEntryLoader.gson.fromJson(reader, SpawnPresets.class);
        for (final JsonObject preset : database.presets)
            if (preset.has("preset_name")) StructureSpawnPresetLoader.presetMap.put(preset.get("preset_name").getAsString(), preset);
            else PokecubeCore.LOGGER.error("Warning, needs a \"preset_name\" field for " + preset);
        reader.close();
    }
}
