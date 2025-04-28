package pokecube.legends.conditions.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.PokecubeCore;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.AbstractEntriedCondition;
import pokecube.legends.conditions.AbstractTypedCondition;
import pokecube.legends.conditions.AndCondition;
import pokecube.legends.conditions.OrCondition;
import pokecube.legends.spawns.LegendarySpawn;
import pokecube.mobs.moves.world.ActionTeleport;
import thut.api.item.ItemList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Conditions
{
    public static Map<String, Consumer<IPokemob>> FAILURE_EFFECTS = new HashMap<>();

    static
    {
        FAILURE_EFFECTS.put("random_teleport", pokemob -> ActionTeleport.teleportRandomly(pokemob.getEntity()));
    }

    public static class Spawn
    {
        public Map<String, String> key = Maps.newHashMap();
        public Map<String, String> target = Maps.newHashMap();
        public SpawnRule location = null;

        private Predicate<ItemStack> _key;
        private Predicate<BlockState> _target;

        public Predicate<ItemStack> getKey()
        {
            if (this._key == null) if (this.key.containsKey("id"))
            {
                final ResourceLocation loc = ResourceLocation.parse(this.key.get("id"));
                Item b = BuiltInRegistries.ITEM.get(loc);
                if (b == null) PokecubeAPI.LOGGER.error("Error loading Legendary Spawn, item {} not found!", loc);
                else if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registering Spawn Key: {}", loc);
                this._key = i -> ItemList.is(loc, i);
            }
            else if (this.key.containsKey("tag"))
            {
                final ResourceLocation loc = ResourceLocation.parse(this.key.get("tag"));
                this._key = i -> ItemList.is(loc, i);
            }
            return this._key;
        }

        public Predicate<BlockState> getTarget()
        {
            if (this._target == null) if (this.target.containsKey("id"))
            {
                final ResourceLocation loc = ResourceLocation.parse(this.target.get("id"));
                Block b = BuiltInRegistries.BLOCK.get(loc);
                if (b == null) PokecubeAPI.LOGGER.error("Error loading Legendary Spawn, block {} not found!", loc);
                else if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registering Spawner: {}", loc);
                this._target = i -> ItemList.is(loc, i);
            }
            else if (this.target.containsKey("tag"))
            {
                final ResourceLocation loc = ResourceLocation.parse(this.target.get("tag"));
                this._target = i -> ItemList.is(loc, i);
            }
            return this._target;
        }
    }

    public static class PresetCondition
    {
        public String name;
        public String preset;

        public JsonObject options = new JsonObject();

        public Spawn spawn;

        public AbstractCondition init()
        {
            return null;
        }

        public void register()
        {
            var cond = this.init();
            if (cond == null) return;
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                if (this.spawn != null && !this.spawn.key.isEmpty() && !this.spawn.target.isEmpty())
                {
                    final LegendarySpawn spawn = new LegendarySpawn(this.name, this.spawn, true);
                    LegendarySpawn.data_spawns.add(spawn);
                }
            }
        }
    }

    public static class OrPreset extends PresetCondition
    {
        JsonObject A;
        JsonObject B;

        @Override
        public AbstractCondition init()
        {
            var _A = ConditionLoader.fromJson(A).init();
            var _B = ConditionLoader.fromJson(B).init();
            var cond = new OrCondition(_A, _B);
            cond.onFail = FAILURE_EFFECTS.getOrDefault(options.get("on_fail").getAsString(), cond.onFail);
            cond.customFailMesg = options.get("failure_message").getAsString();
            return super.init();
        }
    }

    public static class AndPreset extends PresetCondition
    {
        JsonObject A;
        JsonObject B;

        @Override
        public AbstractCondition init()
        {
            var _A = ConditionLoader.fromJson(A).init();
            var _B = ConditionLoader.fromJson(B).init();
            var cond = new AndCondition(_A, _B);
            cond.onFail = FAILURE_EFFECTS.getOrDefault(options.get("on_fail").getAsString(), cond.onFail);
            cond.customFailMesg = options.get("failure_message").getAsString();
            return super.init();
        }
    }

    public static class EntriedCondition extends PresetCondition
    {
        private static class Condition extends AbstractEntriedCondition
        {
            public Condition(final String entry, final String[] needed)
            {
                super(entry, needed);
            }
        }

        @Override
        public AbstractCondition init()
        {
            final String names = this.options.get("entries").getAsString();
            if (names == null)
            {
                PokecubeAPI.LOGGER.error("Warning, No entries found for legendary condition for {}", this.name);
                return null;
            }
            final String[] list = names.split(",");
            final Condition cond = new Condition(this.name, list);
            if (this.options.has("on_fail"))
                cond.onFail = FAILURE_EFFECTS.getOrDefault(options.get("on_fail").getAsString(), cond.onFail);
            cond.customFailMesg = options.get("failure_message").getAsString();
            return cond;
        }
    }

    public static class TypedCondition extends PresetCondition
    {
        private static class Condition extends AbstractTypedCondition
        {
            public Condition(final String name, final String type, final float threshold)
            {
                super(name, type, threshold);
            }
        }

        @Override
        public AbstractCondition init()
        {
            final String type = this.options.get("type").getAsString();
            if (type == null)
            {
                PokecubeAPI.LOGGER.error("Warning, No type found for legendary condition for {}", this.name);
                return null;
            }
            float threshold = 0.5f;
            try
            {
                if (this.options.has("threshold"))
                    threshold = Float.parseFloat(this.options.get("threshold").getAsString());
            }
            catch (final NumberFormatException e1)
            {
                PokecubeAPI.LOGGER.error("Warning, Error with threshold for {}", this.name);
            }
            final Condition cond = new Condition(this.name, type, threshold);
            if (this.options.has("on_fail"))
                cond.onFail = FAILURE_EFFECTS.getOrDefault(options.get("on_fail").getAsString(), cond.onFail);
            cond.customFailMesg = options.get("failure_message").getAsString();
            return cond;
        }
    }

    public List<PresetCondition> conditions = Lists.newArrayList();

    public boolean replace = false;

}
