package pokecube.core.database.stats;

import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.utils.PokeType;

public class SpecialCaseRegister
{
    public static int countSpawnableTypes(PokeType type)
    {
        int ret = 0;
        for (final PokedexEntry e : Database.spawnables)
            if (type == null || e.isType(type)) ret++;
        return ret;
    }

    public static ISpecialCaptureCondition getCaptureCondition(PokedexEntry entry)
    {
        if (entry != null && ISpecialCaptureCondition.captureMap.containsKey(entry))
            return ISpecialCaptureCondition.captureMap.get(entry);
        return null;
    }

    public static ISpecialCaptureCondition getCaptureCondition(String name)
    {
        return SpecialCaseRegister.getCaptureCondition(Database.getEntry(name));
    }

    public static ISpecialSpawnCondition getSpawnCondition(PokedexEntry entry)
    {
        if (entry != null && ISpecialSpawnCondition.spawnMap.containsKey(entry)) return ISpecialSpawnCondition.spawnMap
                .get(entry);
        return null;
    }

    public static ISpecialSpawnCondition getSpawnCondition(String name)
    {
        return SpecialCaseRegister.getSpawnCondition(Database.getEntry(name));
    }

    public static void register(String name, ISpecialCaptureCondition condition)
    {
        if (Database.entryExists(name)) ISpecialCaptureCondition.captureMap.put(Database.getEntry(name), condition);
    }

    public static void register(String name, ISpecialSpawnCondition condition)
    {
        if (Database.entryExists(name)) ISpecialSpawnCondition.spawnMap.put(Database.getEntry(name), condition);
    }
}
