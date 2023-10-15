package pokecube.api.data.pokedex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.InteractsAndEvolutions.Evolution;
import pokecube.api.data.pokedex.conditions.AtLeastLevel;
import pokecube.api.data.pokedex.conditions.AtLocation;
import pokecube.api.data.pokedex.conditions.HasAbility;
import pokecube.api.data.pokedex.conditions.HasHeldItem;
import pokecube.api.data.pokedex.conditions.HasMove;
import pokecube.api.data.pokedex.conditions.IsEntry;
import pokecube.api.data.pokedex.conditions.IsHappy;
import pokecube.api.data.pokedex.conditions.IsSexe;
import pokecube.api.data.pokedex.conditions.IsTraded;
import pokecube.api.data.pokedex.conditions.PokemobCondition;
import pokecube.api.data.pokedex.conditions.RandomChance;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.resources.PackFinder;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class EvolutionDataLoader extends ResourceData
{
    public static void init()
    {
        PokemobCondition.CONDITIONS.put("level", AtLeastLevel.class);
        PokemobCondition.CONDITIONS.put("location", AtLocation.class);
        PokemobCondition.CONDITIONS.put("item", HasHeldItem.class);
        PokemobCondition.CONDITIONS.put("traded", IsTraded.class);
        PokemobCondition.CONDITIONS.put("happy", IsHappy.class);
        PokemobCondition.CONDITIONS.put("sexe", IsSexe.class);
        PokemobCondition.CONDITIONS.put("move", HasMove.class);
        PokemobCondition.CONDITIONS.put("chance", RandomChance.class);
        PokemobCondition.CONDITIONS.put("entry", IsEntry.class);
        PokemobCondition.CONDITIONS.put("ability", HasAbility.class);
    }

    public static final EvolutionDataLoader INSTANCE = new EvolutionDataLoader("database/pokemobs/evolutions/");

    public static Map<PokedexEntry, List<Evolution>> RULES = new HashMap<>();

    private final String tagPath;

    public boolean validLoad = false;

    public EvolutionDataLoader(String key)
    {
        super(key);
        this.tagPath = key;
        DataHelpers.addDataType(this);
    }

    @Override
    public void reload(AtomicBoolean valid)
    {
        this.validLoad = false;
        final String path = new ResourceLocation(this.tagPath).getPath();
        final Map<ResourceLocation, Resource> resources = PackFinder.getJsonResources(path);
        RULES.clear();
        preLoad();
        resources.forEach((l, r) -> this.loadFile(l, r));
        if (this.validLoad)
        {
            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded Pokemob spawns.");
            valid.set(true);
        }
    }

    private void loadFromJson(JsonElement element)
    {
        var rule = JsonUtil.gson.fromJson(element, Evolution.class);
        PokedexEntry result = Database.getEntry(rule.name);
        if (result == null)
        {
            PokecubeAPI.LOGGER.error("Needs result for a evo! {}", element);
            return;
        }
        PokedexEntry user = Database.getEntry(rule.user);
        if (user == null)
        {
            PokecubeAPI.LOGGER.error("Needs user for a evo! {}", element);
            return;
        }

        List<Evolution> rules = RULES.get(user);
        if (rules == null) RULES.put(user, rules = new ArrayList<>());
        rules.add(rule);
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
            JsonElement e = JsonUtil.gson.fromJson(reader, JsonElement.class);
            // We load multiple from file
            if (e.isJsonArray())
            {
                var arr = e.getAsJsonArray();
                arr.forEach(this::loadFromJson);
            }
            // Otherwise we load 1
            else if (e.isJsonObject())
            {
                this.loadFromJson(e);
            }
            else throw new IllegalArgumentException(l.toString());
        }
        catch (Exception e)
        {
            // Might not be valid, so log and skip in that case.
            PokecubeAPI.LOGGER.error("Error with resources in {}", l);
            PokecubeAPI.LOGGER.error(e);
        }
    }
}
