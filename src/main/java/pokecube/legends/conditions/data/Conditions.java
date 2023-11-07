package pokecube.legends.conditions.data;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.PokecubeCore;
import pokecube.legends.conditions.AbstractEntriedCondition;
import pokecube.legends.conditions.AbstractTypedCondition;
import pokecube.legends.spawns.LegendarySpawn;
import thut.api.item.ItemList;

public class Conditions
{

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
                final ResourceLocation loc = new ResourceLocation(this.key.get("id"));
                Item b = ForgeRegistries.ITEMS.getValue(loc);
                if (b == null) PokecubeAPI.LOGGER.error("Error loading Legendary Spawn, item {} not found!", loc);
                else if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registering Spawn Key: {}", loc);
                this._key = i -> ItemList.is(loc, i);
            }
            else if (this.key.containsKey("tag"))
            {
                final ResourceLocation loc = new ResourceLocation(this.key.get("tag"));
                this._key = i -> ItemList.is(loc, i);
            }
            return this._key;
        }

        public Predicate<BlockState> getTarget()
        {
            if (this._target == null) if (this.target.containsKey("id"))
            {
                final ResourceLocation loc = new ResourceLocation(this.target.get("id"));
                Block b = ForgeRegistries.BLOCKS.getValue(loc);
                if (b == null) PokecubeAPI.LOGGER.error("Error loading Legendary Spawn, block {} not found!", loc);
                else if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Registering Spawner: {}", loc);
                this._target = i -> ItemList.is(loc, i);
            }
            else if (this.target.containsKey("tag"))
            {
                final ResourceLocation loc = new ResourceLocation(this.target.get("tag"));
                this._target = i -> ItemList.is(loc, i);
            }
            return this._target;
        }
    }

    public static class PresetCondition
    {
        public String name;
        public String preset;

        public Map<String, String> options = Maps.newHashMap();

        public Spawn spawn;

        public void register()
        {
            if (this.spawn != null && !this.spawn.key.isEmpty() && !this.spawn.target.isEmpty())
            {
                final LegendarySpawn spawn = new LegendarySpawn(this.name, this.spawn, true);
                LegendarySpawn.data_spawns.add(spawn);
            }
        };
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
        public void register()
        {
            final String names = this.options.get("entries");
            if (names == null)
            {
                PokecubeAPI.LOGGER
                        .error(String.format("Warning, No entries found for legendary condition for {}", this.name));
                return;
            }
            final String[] list = names.split(",");
            final Condition cond = new Condition(this.name, list);
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                super.register();
            }
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
        public void register()
        {
            final String type = this.options.get("type");
            if (type == null)
            {
                PokecubeAPI.LOGGER
                        .error(String.format("Warning, No type found for legendary condition for {}", this.name));
                return;
            }
            float threshold = 0.5f;
            try
            {
                if (this.options.containsKey("threshold")) threshold = Float.parseFloat(this.options.get("threshold"));
            }
            catch (final NumberFormatException e1)
            {
                PokecubeAPI.LOGGER.error(String.format("Warning, Error with threshold for {}", this.name));
            }
            final Condition cond = new Condition(this.name, type, threshold);
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                super.register();
            }
        }
    }

    public List<PresetCondition> conditions = Lists.newArrayList();

    public boolean replace = false;

}
