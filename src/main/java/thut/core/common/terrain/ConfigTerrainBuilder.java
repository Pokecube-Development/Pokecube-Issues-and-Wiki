package thut.core.common.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;

public class ConfigTerrainBuilder
{
    private static void addToList(final List<Predicate<BlockState>> list, final String... conts)
    {
        if (conts == null) return;
        if (conts.length < 1) return;
        for (final String s : conts)
        {
            final Predicate<BlockState> b = ConfigTerrainBuilder.getState(s);
            if (b != null) list.add(b);
        }

    }

    private static void generateConfigTerrain(final String[] blocks, final BiomeType subbiome)
    {
        final List<Predicate<BlockState>> list = Lists.newArrayList();
        ConfigTerrainBuilder.addToList(list, blocks);
        if (!list.isEmpty())
        {
            final ConfigTerrainChecker checker = new ConfigTerrainChecker(list, subbiome);
            TerrainSegment.biomeCheckers.add(checker);
        }
    }

    public static Predicate<BlockState> getState(final String arguments)
    {
        final String[] args = arguments.split(" ");

        final String[] resource = args[0].split(":");
        final String modid = resource[0];
        final String blockName = resource[1];
        String keyTemp = null;
        String valTemp = null;

        if (args.length > 1)
        {
            final String[] state = args[1].split("=");
            keyTemp = state[0];
            valTemp = state[1];
        }
        final String key = keyTemp;
        final String val = valTemp;
        return new Predicate<BlockState>()
        {
            final Pattern                  modidPattern = Pattern.compile(modid);
            final Pattern                  blockPattern = Pattern.compile(blockName);
            Map<ResourceLocation, Boolean> checks       = Maps.newHashMap();

            @Override
            public boolean apply(final BlockState input)
            {
                if (input == null || input.getBlock() == null) return false;
                final Block block = input.getBlock();
                final ResourceLocation name = block.getRegistryName();
                if (this.checks.containsKey(name) && !this.checks.get(name)) return false;
                else if (!this.checks.containsKey(name))
                {
                    if (!this.modidPattern.matcher(name.getNamespace()).matches())
                    {
                        this.checks.put(name, false);
                        return false;
                    }
                    if (!this.blockPattern.matcher(name.getPath()).matches())
                    {
                        this.checks.put(name, false);
                        return false;
                    }
                    this.checks.put(name, true);
                }
                if (key == null) return true;
                for (final Property<?> prop : input.getProperties())
                    if (prop.getName().equals(key))
                    {
                        final Object inputVal = input.getValue(prop);
                        return inputVal.toString().equalsIgnoreCase(val);
                    }
                return false;
            }
        };
    }

    public static void process(final List<String> values)
    {
        final Map<String, ArrayList<String>> types = Maps.newHashMap();
        for (final String s : values)
            try
            {
                final String[] args = s.split("->");
                final String id = args[0];
                final String val = args[1];
                ArrayList<String> list = types.get(id);
                if (list == null) types.put(id, list = Lists.newArrayList());
                list.add(val);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        for (final String type : types.keySet())
        {
            final BiomeType subbiome = BiomeType.getBiome(type, true);
            ConfigTerrainBuilder.generateConfigTerrain(types.get(type).toArray(new String[0]), subbiome);
        }
    }

}
