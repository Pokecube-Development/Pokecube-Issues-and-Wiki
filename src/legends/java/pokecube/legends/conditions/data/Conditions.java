package pokecube.legends.conditions.data;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3i;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.AbstractEntriedCondition;
import pokecube.legends.conditions.AbstractTypedCondition;
import pokecube.legends.conditions.AndCondition;
import pokecube.legends.conditions.OrCondition;
import pokecube.legends.spawns.LegendarySpawn;
import pokecube.mobs.moves.world.ActionTeleport;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.lib.RegHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Conditions
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
        public Spawn spawn;

        public String failure_effect;
        public String failure_message;

        public AbstractCondition init()
        {
            return null;
        }

        public AbstractCondition register()
        {
            var cond = this.init();
            if (cond == null) return null;
            if (this.failure_effect != null)
                cond.onFail = FAILURE_EFFECTS.getOrDefault(this.failure_effect, cond.onFail);
            cond.customFailMesg = this.failure_message;
            final PokedexEntry e = Database.getEntry(this.name);
            if (Pokedex.getInstance().isRegistered(e))
            {
                cond.setEntry(e);
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                if (this.spawn != null && !this.spawn.key.isEmpty() && !this.spawn.target.isEmpty())
                {
                    final LegendarySpawn spawn = new LegendarySpawn(this.name, this.spawn, true);
                    LegendarySpawn.data_spawns.add(spawn);
                }
                return cond;
            }
            return null;
        }
    }

    public static class OrPreset extends PresetCondition
    {
        JsonElement conditions;

        @Override
        public AbstractCondition init()
        {
            List<AbstractCondition> list = new ArrayList<>();
            conditions.getAsJsonArray().asList()
                    .forEach(element -> list.add(ConditionLoader.fromJson(element.getAsJsonObject()).init()));
            if (list.isEmpty())
            {
                return null;
            }
            return new OrCondition(list);
        }
    }

    public static class AndPreset extends PresetCondition
    {
        JsonElement conditions;

        @Override
        public AbstractCondition init()
        {
            List<AbstractCondition> list = new ArrayList<>();
            conditions.getAsJsonArray().asList()
                    .forEach(element -> list.add(ConditionLoader.fromJson(element.getAsJsonObject()).init()));
            if (list.isEmpty())
            {
                return null;
            }
            return new AndCondition(list);
        }
    }

    public static class EntriedCondition extends PresetCondition
    {
        List<String> entries = new ArrayList<>();

        private static class Condition extends AbstractEntriedCondition
        {
            public Condition(String[] needed)
            {
                super(needed);
            }
        }

        @Override
        public AbstractCondition init()
        {
            if (this.entries.isEmpty())
            {
                PokecubeAPI.LOGGER.error("Warning, No entries found for legendary condition for {}", this.name);
                return null;
            }
            return new Condition(this.entries.toArray(new String[0]));
        }
    }

    public static class TypedCondition extends PresetCondition
    {
        private static class Condition extends AbstractTypedCondition
        {
            public Condition(String type, float threshold, boolean capture)
            {
                super(type, threshold, capture);
            }
        }

        double threshold = 0.5;
        String poketype;
        boolean capture = true;

        @Override
        public AbstractCondition init()
        {
            if (poketype == null)
            {
                PokecubeAPI.LOGGER.error("Warning, No type found for legendary condition for {}", this.name);
                return null;
            }
            return new Condition(poketype, (float) threshold, capture);
        }
    }

    public static class BuiltCondition extends PresetCondition
    {
        public static class Slice
        {
            List<String> rows = new ArrayList<>();

            public Map<Vector3i, Key> toPoints(int y, Map<String, Key> keys)
            {
                Map<Vector3i, Key> values = new HashMap<>();
                for (int z = 0; z < rows.size(); z++)
                {
                    var row = this.rows.get(z);
                    for (int x = 0; x < row.length(); x++)
                    {
                        var c = String.valueOf(row.charAt(x));
                        // blanks or placeholders for air, etc
                        if (!keys.containsKey(c)) continue;
                        Key k = keys.get(c);
                        values.put(new Vector3i(x, y, z), k);
                    }
                }
                return values;
            }
        }

        public static class Key implements Predicate<BlockState>
        {
            String block;
            String tag;

            TagKey<Block> _tag;
            ResourceLocation _name;

            @Override
            public boolean test(BlockState state)
            {
                if (this.tag != null)
                {
                    if (_tag == null) _tag = BlockTags.create(ResourceLocation.parse(tag));
                    return state.is(_tag);
                }
                if (_name == null) _name = ResourceLocation.parse(block);
                return RegHelper.getKey(state.getBlock()).equals(_name);
            }
        }

        public static class Build
        {
            List<Slice> pattern = new ArrayList<>();
            Map<String, Key> key = new HashMap<>();
            String root_key;

            private Map<Vector3i, Key> _processed;

            private Map<Vector3i, Key> toList()
            {
                Map<Vector3i, Key> values = new HashMap<>();
                var root = key.get(root_key);
                Vector3i root_shift = new Vector3i(0);
                for (int i = 0; i < pattern.size(); i++)
                {
                    var slice = pattern.get(i);
                    int y = -i;
                    var points = slice.toPoints(y, key);
                    if (points.containsValue(root))
                    {
                        points.forEach((v, k) -> {
                            if (k.equals(root)) root_shift.set(v);
                        });
                        root_shift.y = y;
                    }
                    values.putAll(points);
                }
                values.keySet().forEach(v -> v.sub(root_shift));
                return values;
            }

            private List<BlockPos> test(ServerLevel level, BlockPos root, Direction dir)
            {
                List<BlockPos> points = new ArrayList<>();
                for (var entry : _processed.entrySet())
                {
                    var dr = entry.getKey();
                    // Copy the vector
                    var _dr = new Vector3i(dr);
                    switch (dir)
                    {
                    // East is default
                    case EAST:
                    {
                        break;
                    }
                    case NORTH:
                    {
                        _dr.x = -dr.z;
                        _dr.z = dr.x;
                        break;
                    }
                    case WEST:
                    {
                        _dr.x = -dr.x;
                        _dr.z = -dr.z;
                        break;
                    }
                    case SOUTH:
                    {
                        _dr.x = dr.z;
                        _dr.z = -dr.x;
                        break;
                    }
                    }
                    var key = entry.getValue();
                    BlockPos test = root.offset(_dr.x, _dr.y, _dr.z);
                    BlockState state = level.getBlockState(test);
                    points.add(test);
                    if (!key.test(state)) return null;
                }
                return points;
            }

            public boolean matches(ServerLevel level, BlockPos root)
            {
                if (_processed == null) _processed = toList();
                // we try in horizontal plane of rotations
                return Direction.Plane.HORIZONTAL.stream().anyMatch(d -> test(level, root, d) != null);
            }

            public boolean consume(ServerLevel level, BlockPos root)
            {
                return Direction.Plane.HORIZONTAL.stream().anyMatch(d -> {
                    var list = test(level, root, d);
                    if (list == null) return false;
                    list.forEach(pos -> level.removeBlock(pos, false));
                    return true;
                });
            }
        }

        public static class Condition extends AbstractCondition
        {
            private final Build build;
            private final int level;

            public Condition(Build build, int level)
            {
                this.build = build;
                this.level = level;
                build.key.values().forEach(this::setRelevant);
            }

            @Override
            protected boolean hasRequirements(Entity trainer)
            {
                return true;
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setForSpawn(Tools.levelToXp(mob.getPokedexEntry().getEvolutionMode(), this.level));
                super.onSpawn(mob);
                Vector3 location = new Vector3().set(mob.getEntity()).add(0, -1, 0);
                this.build.consume((ServerLevel) mob.getEntity().level(), location.getPos());
            }

            @Override
            public CanSpawn canSpawn(SpawnEvent.SpawnContext context, boolean message)
            {
                final CanSpawn test = super.canSpawn(context, message);
                if (!test.test()) return test;
                boolean check = build.matches(context.level(), context.location().getPos());
                if (!check)
                {
                    if (message)
                    {
                        this.sendLegendBuild(context.player(),
                                Component.translatable(this.getEntry().getUnlocalizedName()));
                    }
                    return CanSpawn.NO;
                }
                return CanSpawn.YES;
            }
        }

        int level = 40;
        Build build;

        @Override
        public AbstractCondition init()
        {
            if (build == null)
            {
                PokecubeAPI.LOGGER.error("Warning, No build found for legendary condition for {}", this.name);
                return null;
            }
            return new Condition(build, level);
        }
    }
}
