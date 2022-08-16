package pokecube.api.stats;

import java.util.Map;
import java.util.UUID;

import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.eventhandlers.StatsCollector;

public class EggStats
{
    public static int getNumberUniqueHatchedBy(UUID playerID)
    {
        int count = 0;
        final Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerID);
        if (map == null) return 0;
        count += map.size();
        return count;
    }

    public static int getTotalNumberHatchedBy(UUID playerID)
    {
        int count = 0;
        final Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerID);
        if (map == null) return 0;
        for (final Integer i : map.values())
            count += i;
        return count;
    }

    public static int getTotalNumberOfPokemobHatchedBy(UUID playerID, PokedexEntry type)
    {
        int count = 0;
        final Map<PokedexEntry, Integer> map = StatsCollector.getHatches(playerID);
        if (map == null) return 0;
        if (map.containsKey(type)) count += map.get(type);
        return count;
    }

    public static int getTotalOfTypeHatchedBy(UUID player, PokeType type)
    {
        int count = 0;
        for (final PokedexEntry dbe : StatsCollector.getHatches(player).keySet())
            if (dbe.isType(type)) count += StatsCollector.getHatches(player).get(dbe);
        return count;
    }

    public static int getUniqueOfTypeHatchedBy(UUID player, PokeType type)
    {
        int count = 0;
        for (final PokedexEntry dbe : StatsCollector.getHatches(player).keySet())
            if (dbe.isType(type)) count++;
        return count;
    }
}
