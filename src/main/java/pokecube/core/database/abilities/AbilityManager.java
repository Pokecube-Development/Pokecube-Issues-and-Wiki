package pokecube.core.database.abilities;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import thut.lib.CompatParser.ClassFinder;

@SuppressWarnings("unchecked")
public class AbilityManager
{

    private static HashMap<String, Class<? extends Ability>>  nameMap  = Maps.newHashMap();
    private static HashMap<Class<? extends Ability>, String>  nameMap2 = Maps.newHashMap();
    private static HashMap<Class<? extends Ability>, Integer> idMap    = Maps.newHashMap();
    private static HashMap<Integer, Class<? extends Ability>> idMap2   = Maps.newHashMap();
    public static Set<Package>                                packages = Sets.newHashSet();

    static
    {
        AbilityManager.packages.add(AbilityManager.class.getPackage());
    }

    static int nextID = 0;

    public static boolean abilityExists(String name)
    {
        if (name == null) return false;
        name = name.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", "");
        return AbilityManager.nameMap.containsKey(name);
    }

    public static void addAbility(final Class<? extends Ability> ability)
    {
        AbilityManager.addAbility(ability, ability.getSimpleName());
    }

    public static void addAbility(final Class<? extends Ability> ability, String name)
    {
        name = name.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", "");
        AbilityManager.nameMap.put(name, ability);
        AbilityManager.nameMap2.put(ability, name);
        AbilityManager.idMap.put(ability, AbilityManager.nextID);
        AbilityManager.idMap2.put(AbilityManager.nextID, ability);
        AbilityManager.nextID++;
    }

    public static Ability getAbility(final Integer id, final Object... args)
    {
        return AbilityManager.makeAbility(id, args);
    }

    public static Ability getAbility(String name, final Object... args)
    {
        if (name == null) return null;
        if (name.startsWith("ability.")) name = name.substring(7);
        if (name.endsWith(".name")) name = name.substring(0, name.length() - 5);
        return AbilityManager.makeAbility(name.toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "")
                .replaceAll(" ", ""), args);
    }

    public static int getIdForAbility(final Ability ability)
    {
        return AbilityManager.idMap.get(ability.getClass());
    }

    public static String getNameForAbility(final Ability ability)
    {
        return AbilityManager.nameMap2.get(ability.getClass());
    }

    public static boolean hasAbility(final String abilityName, final IPokemob pokemob)
    {
        final Ability ability = pokemob.getAbility();
        if (ability == null) return false;
        return ability.toString().equalsIgnoreCase(abilityName.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll(
                "[^\\w\\s ]", "").replaceAll(" ", ""));
    }

    public static void init()
    {
        List<Class<?>> foundClasses;
        try
        {
            int num = 0;
            for (final Package pack : AbilityManager.packages)
            {
                if (pack == null) continue;
                foundClasses = ClassFinder.find(pack.getName());
                for (final Class<?> candidateClass : foundClasses)
                    if (Ability.class.isAssignableFrom(candidateClass) && candidateClass != Ability.class)
                    {
                        num++;
                        AbilityManager.addAbility((Class<? extends Ability>) candidateClass);
                    }
            }
            PokecubeCore.LOGGER.debug("Registered " + num + " Abilities");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Ability makeAbility(final Object val, final Object... args)
    {
        Class<? extends Ability> abil = null;
        if (val instanceof String) abil = AbilityManager.nameMap.get(val);
        else if (val instanceof Class) abil = (Class<? extends Ability>) val;
        else abil = AbilityManager.idMap2.get(val);
        if (abil == null) return null;
        Ability ret = null;
        try
        {
            ret = abil.newInstance().init(args);
            ret.init(args);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static void replaceAbility(final Class<? extends Ability> ability, String name)
    {
        name = name.trim().toLowerCase(java.util.Locale.ENGLISH).replaceAll("[^\\w\\s ]", "").replaceAll(" ", "");
        if (AbilityManager.nameMap.containsKey(name))
        {
            final Class<? extends Ability> old = AbilityManager.nameMap.remove(name);
            AbilityManager.nameMap2.remove(old);
            final int id = AbilityManager.idMap.remove(old);
            AbilityManager.nameMap.put(name, ability);
            AbilityManager.nameMap2.put(ability, name);
            AbilityManager.idMap.put(ability, id);
            AbilityManager.idMap2.put(id, ability);
        }
        else AbilityManager.addAbility(ability, name);
    }
}
