package pokecube.legends.conditions.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.legends.conditions.AbstractEntriedCondition;
import pokecube.legends.conditions.AbstractTypedCondition;

public class Conditions
{

    public static class PresetCondition
    {
        public String name;
        public String preset;

        public Map<String, String> options = Maps.newHashMap();
        public Map<String, String> spawn   = Maps.newHashMap();

        public void register()
        {
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
                PokecubeCore.LOGGER.error(String.format("Warning, No entries found for legendary condition for {}",
                        this.name));
                return;
            }
            final String[] list = names.split(",");
            final Condition cond = new Condition(this.name, list);
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
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
                PokecubeCore.LOGGER.error(String.format("Warning, No type found for legendary condition for {}",
                        this.name));
                return;
            }
            float threshold = 0.5f;
            try
            {
                if (this.options.containsKey("threshold")) threshold = Float.parseFloat(this.options.get("threshold"));
            }
            catch (final NumberFormatException e1)
            {
                PokecubeCore.LOGGER.error(String.format("Warning, Error with threshold for {}", this.name));
            }
            final Condition cond = new Condition(this.name, type, threshold);
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
            }
        }
    }

    public List<PresetCondition> conditions = Lists.newArrayList();

    public boolean replace = false;

}
