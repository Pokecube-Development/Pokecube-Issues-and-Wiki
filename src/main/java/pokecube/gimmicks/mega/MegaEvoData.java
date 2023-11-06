package pokecube.gimmicks.mega;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.resources.PackFinder;
import pokecube.gimmicks.mega.conditions.Ability;
import pokecube.gimmicks.mega.conditions.HeldItem;
import pokecube.gimmicks.mega.conditions.MegaCondition;
import pokecube.gimmicks.mega.conditions.Move;
import thut.api.data.DataHelpers;
import thut.api.data.DataHelpers.ResourceData;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class MegaEvoData extends ResourceData
{
    public static interface MegaRule
    {
        boolean matches(IPokemob mobIn);

        PokedexEntry getResult();
    }

    public static class BaseRuleJson implements MegaRule
    {
        public String name;
        public String user;
        public JsonElement rule;
        // If true, this rule will always revert when recalled, even if it
        // "naturally" spawned.
        public boolean auto_revert = true;

        private MegaCondition _condition;
        private PokedexEntry _entryTo;

        public void init()
        {
            if (name == null)
            {
                PokecubeAPI.LOGGER.error("Needs name for a mega evo!");
                return;
            }
            if (user == null)
            {
                PokecubeAPI.LOGGER.error("Needs user for a mega evo!");
                return;
            }
            if (rule == null)
            {
                PokecubeAPI.LOGGER.error("Needs rule for a mega evo!");
                return;
            }
            this._entryTo = Database.getEntry(this.name);
            if (_entryTo == null)
            {
                PokecubeAPI.LOGGER.error("invalid name {} for a mega evo rule!", name);
                return;
            }
            PokedexEntry user = Database.getEntry(this.user);
            if (user == null)
            {
                PokecubeAPI.LOGGER.error("invalid user {} for a mega evo rule!", user);
                return;
            }
            this._condition = MegaCondition.makeFromElement(rule);
            if (this._condition == null)
            {
                PokecubeAPI.LOGGER.error("invalid rule {} for a mega evo rule!", rule);
                return;
            }
            List<MegaRule> rules = RULES.get(user);
            if (rules == null) RULES.put(user, rules = new ArrayList<>());
            rules.add(this);
            if (auto_revert) REVERSIONS.put(_entryTo, user);
        }

        @Override
        public boolean matches(IPokemob mobIn)
        {
            if (_condition != null) return _condition.matches(mobIn, _entryTo);
            return false;
        }

        @Override
        public PokedexEntry getResult()
        {
            return _entryTo;
        }
    }

    public static final MegaEvoData INSTANCE = new MegaEvoData("database/pokemobs/mega_evos/");

    public static Map<PokedexEntry, List<MegaRule>> RULES = new HashMap<>();
    public static Map<PokedexEntry, PokedexEntry> REVERSIONS = new HashMap<>();

    public static void init()
    {
        MegaCondition.CONDITIONS.put("item", HeldItem.class);
        MegaCondition.CONDITIONS.put("ability", Ability.class);
        MegaCondition.CONDITIONS.put("move", Move.class);
    }

    public static PokedexEntry getMegaEvo(IPokemob pokemob)
    {
        List<MegaRule> rules = RULES.getOrDefault(pokemob.getPokedexEntry(), Collections.emptyList());
        if (rules.isEmpty()) return null;
        Collections.shuffle(rules);
        for (var rule : rules) if (rule.matches(pokemob)) return rule.getResult();
        return null;
    }

    private final String tagPath;

    public boolean validLoad = false;

    public MegaEvoData(String key)
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
        var rule = JsonUtil.gson.fromJson(element, BaseRuleJson.class);
        rule.init();
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
