package pokecube.core.gimmicks.mega;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.resources.PackFinder;
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

    public static interface MegaCondition
    {
        boolean matches(IPokemob mobIn, PokedexEntry entryTo);

        void init();
    }

    public static class Ability implements MegaCondition
    {
        public String ability;

        @Override
        public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
        {
            return AbilityManager.hasAbility(this.ability, mobIn);
        }

        @Override
        public void init()
        {}

    }

    public static class Move implements MegaCondition
    {
        public String move;

        @Override
        public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
        {
            return Tools.hasMove(this.move, mobIn);
        }

        @Override
        public void init()
        {}

    }

    public static class HeldItem implements MegaCondition
    {
        public JsonObject item;
        public String tag = "";
        private ItemStack _value = ItemStack.EMPTY;
        private TagKey<Item> _tag = null;

        @Override
        public boolean matches(IPokemob mobIn, PokedexEntry entryTo)
        {
            if (_tag != null && mobIn.getHeldItem().is(_tag)) return true;
            if (!this._value.isEmpty())
            {
                boolean rightStack = Tools.isSameStack(this._value, mobIn.getHeldItem(), true);
                if (!rightStack) rightStack = MegaCapability.matches(mobIn.getHeldItem(), entryTo);
                return rightStack;
            }
            return false;
        }

        @Override
        public void init()
        {
            if (item != null) _value = CraftingHelper.getItemStack(item, true, true);
            if (!tag.isEmpty())
            {
                _tag = TagKey.create(Keys.ITEMS, new ResourceLocation(tag));
            }
        }
    }

    public static class BaseRuleJson implements MegaRule
    {
        public String name;
        public String type;
        public String user;
        public JsonElement rule;

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
            if (type == null)
            {
                PokecubeAPI.LOGGER.error("Needs type key for a mega evo rule!");
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
            Class<? extends MegaCondition> condClass = CONDITIONS.get(type);
            if (condClass == null)
            {
                PokecubeAPI.LOGGER.error("invalid type key {} for a mega evo rule!", type);
                return;
            }
            this._condition = JsonUtil.gson.fromJson(rule, condClass);
            this._condition.init();

            List<MegaRule> rules = RULES.get(user);
            if (rules == null) RULES.put(user, rules = new ArrayList<>());
            rules.add(this);
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

    public static Map<String, Class<? extends MegaCondition>> CONDITIONS = new HashMap<>();

    public static Map<PokedexEntry, List<MegaRule>> RULES = new HashMap<>();

    public static void init()
    {
        CONDITIONS.put("item", HeldItem.class);
        CONDITIONS.put("ability", Ability.class);
        CONDITIONS.put("move", Move.class);
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
