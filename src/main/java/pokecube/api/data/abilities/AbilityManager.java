package pokecube.api.data.abilities;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import thut.core.common.ThutCore;
import thut.lib.CompatParser.ClassFinder;

public class AbilityManager
{

    private static Map<String, Class<? extends Ability>> nameMap = Maps.newHashMap();
    private static Map<Class<? extends Ability>, String> nameMap2 = Maps.newHashMap();
    private static Map<Class<? extends Ability>, Ability> singltons = Maps.newHashMap();
    private static Set<Package> packages = Sets.newHashSet();

    public static void registerAbilityPackage(Package pack)
    {
        packages.add(pack);
    }

    static
    {
        registerAbilityPackage(AbilityManager.class.getPackage());
    }

    private static Map<String, String> fixed = Maps.newHashMap();

    private static String getAbilityName(String name)
    {
        if (name == null) return null;
        if (AbilityManager.fixed.containsKey(name)) return AbilityManager.fixed.get(name);
        final String original = name;
        name = ThutCore.trim(name);
        name = name.replace("_", "");
        name = name.replace("-", "");
        AbilityManager.fixed.put(original, name);
        return name;
    }

    public static boolean abilityExists(final String name)
    {
        if (name == null) return false;
        return AbilityManager.nameMap.containsKey(AbilityManager.getAbilityName(name));
    }

    public static void addAbility(final Class<? extends Ability> ability)
    {
        AbilityManager.addAbility(ability, ability.getSimpleName());
    }

    public static void addAbility(final Class<? extends Ability> ability, String name)
    {
        name = AbilityManager.getAbilityName(name);
        AbilityManager.nameMap.put(name, ability);
        AbilityManager.nameMap2.put(ability, name);
    }

    public static Ability getAbility(String name, final Object... args)
    {
        if (name == null) return null;
        if (name.startsWith("ability.")) name = name.substring(7);
        if (name.endsWith(".name")) name = name.substring(0, name.length() - 5);
        return AbilityManager.makeAbility(AbilityManager.getAbilityName(name), args);
    }

    public static String getNameForAbility(final Ability ability)
    {
        return AbilityManager.nameMap2.get(ability.getClass());
    }

    public static boolean hasAbility(final String abilityName, final IPokemob pokemob)
    {
        final Ability ability = pokemob.getAbility();
        if (ability == null) return false;
        return ability.toString().equalsIgnoreCase(AbilityManager.getAbilityName(abilityName));
    }

    @SuppressWarnings("unchecked")
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
                for (final Class<?> candidateClass : foundClasses) if (Ability.class.isAssignableFrom(candidateClass)
                        && !candidateClass.getName().equals(Ability.class.getName()))
                {
                    num++;
                    AbilityManager.addAbility((Class<? extends Ability>) candidateClass);
                }
            }
            PokecubeAPI.LOGGER.debug("Registered " + num + " Abilities");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static Ability makeAbility(final Object val, final Object... args)
    {
        Class<? extends Ability> abil = null;
        if (val instanceof String) abil = AbilityManager.nameMap.get(val);
        else if (val instanceof Class) abil = (Class<? extends Ability>) val;
        if (abil == null) return null;
        Ability ret = null;
        try
        {
            if (AbilityManager.singltons.containsKey(abil))
            {
                return AbilityManager.singltons.get(abil);
            }
            ret = abil.getConstructor().newInstance().init(args);
            ret.init(args);
            if (ret.singleton()) AbilityManager.singltons.put(abil, ret);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static void replaceAbility(final Class<? extends Ability> ability, String name)
    {
        name = AbilityManager.getAbilityName(name);
        if (AbilityManager.nameMap.containsKey(name))
        {
            final Class<? extends Ability> old = AbilityManager.nameMap.remove(name);
            AbilityManager.nameMap2.remove(old);
            AbilityManager.nameMap.put(name, ability);
            AbilityManager.nameMap2.put(ability, name);
        }
        else AbilityManager.addAbility(ability, name);
    }
}
