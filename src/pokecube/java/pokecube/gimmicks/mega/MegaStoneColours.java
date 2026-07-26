package pokecube.gimmicks.mega;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

/**
 * Datapack-provided names and four-layer tint colours for Mega Stones. Files are loaded from
 * {@code data/<namespace>/database/pokemobs/mega_stones/}. Resource stacks are applied from lowest to highest priority,
 * so a datapack can override selected entries without copying the complete built-in index.
 */
public final class MegaStoneColours extends ResourceData
{
    private record Stone(String name, int[] colours)
    {
        int[] toArray()
        {
            return this.colours.clone();
        }
    }

    private static class StoneDefinition
    {
        String name;
        List<String> colours = Collections.emptyList();

        Stone parse(final String entry)
        {
            if (this.name == null || this.name.isBlank())
                throw new IllegalArgumentException("Missing Mega Stone name for " + entry);
            if (this.colours == null || this.colours.size() != 4)
                throw new IllegalArgumentException("Mega Stone " + entry + " must define exactly four colours");
            final int[] parsed = new int[4];
            for (int i = 0; i < parsed.length; i++) parsed[i] = MegaStoneColours.parseColour(this.colours.get(i));
            return new Stone(this.name, parsed);
        }
    }

    private static class StoneFile
    {
        boolean replace = false;
        Map<String, StoneDefinition> values = Collections.emptyMap();
    }

    public static final MegaStoneColours INSTANCE = new MegaStoneColours(
            "database/pokemobs/mega_stones/");

    private static volatile Map<String, Stone> stones = Collections.emptyMap();

    private final String path;

    private MegaStoneColours(final String path)
    {
        super(path);
        this.path = ResourceLocation.parse(path).getPath();
        DataHelpers.addDataType(this);
    }

    /** Forces this datapack loader to be registered during Mega Evolution setup. */
    public static void init()
    {}

    @Override
    public void reload(final AtomicBoolean valid)
    {
        final Map<String, Stone> loaded = new HashMap<>();
        final Map<ResourceLocation, List<Resource>> resources = PackFinder.getAllJsonResources(this.path);
        this.preLoad();
        resources.forEach((location, stack) -> stack.forEach(resource -> this.loadFile(location, resource, loaded)));
        stones = Map.copyOf(loaded);
        if (!loaded.isEmpty())
        {
            if (PokecubeCore.getConfig().debug_data)
                PokecubeAPI.logInfo("Loaded {} Mega Stone colour definitions.", loaded.size());
            valid.set(true);
        }
    }

    private void loadFile(final ResourceLocation location, final Resource resource, final Map<String, Stone> loaded)
    {
        try (BufferedReader reader = ResourceHelper.getReader(resource))
        {
            if (reader == null) throw new FileNotFoundException(location.toString());
            final StoneFile file = JsonUtil.gson.fromJson(reader, StoneFile.class);
            if (file == null || file.values == null)
                throw new IllegalArgumentException("Missing Mega Stone values in " + location);
            if (file.replace) loaded.clear();
            file.values.forEach((entry, definition) -> {
                try
                {
                    loaded.put(entry, definition.parse(entry));
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Invalid Mega Stone definition {} in {}", entry, location, e);
                }
            });
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error loading Mega Stone definitions from {}", location, e);
        }
    }

    private static int parseColour(final String value)
    {
        if (value == null) throw new IllegalArgumentException("Missing colour");
        String hex = value.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        else if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
        if (hex.length() == 6) hex = "FF" + hex;
        if (hex.length() != 8) throw new IllegalArgumentException("Expected #RRGGBB or #AARRGGBB, got " + value);
        return (int) Long.parseUnsignedLong(hex, 16);
    }

    /**
     * @return the datapack-provided name for the Mega Stone, or {@code null} when no stone is known
     */
    public static String getName(final PokedexEntry mega)
    {
        if (mega == null) return null;
        final Stone stone = stones.get(mega.getTrimmedName());
        return stone == null ? null : stone.name();
    }

    /**
     * @return a new four-colour array for the Mega entry, or {@code null} when no stone is known
     */
    public static int[] get(final PokedexEntry mega)
    {
        if (mega == null) return null;
        final Stone stone = stones.get(mega.getTrimmedName());
        return stone == null ? null : stone.toArray();
    }
}
