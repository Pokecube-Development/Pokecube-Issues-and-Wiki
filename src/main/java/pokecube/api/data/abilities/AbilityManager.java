package pokecube.api.data.abilities;

import java.lang.reflect.Modifier;
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
    private static Map<String, Ability> singltons = Maps.newHashMap();
    private static Set<Package> packages = Sets.newHashSet();

    public static void registerAbilityPackage(Package pack)
    {
        packages.add(pack);
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
        Ability ability = AbilityManager.makeAbility(AbilityManager.getAbilityName(name), args);
        return ability;
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
                        && !Modifier.isAbstract(candidateClass.getModifiers()))
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

    public static Ability makeAbility(final String name, final Object... args)
    {
        if (AbilityManager.singltons.containsKey(name))
        {
            return AbilityManager.singltons.get(name);
        }
        Class<? extends Ability> clazz = AbilityManager.nameMap.get(name);
        if (clazz == null) clazz = DummyAbility.class;
        Ability ability = null;
        try
        {
            ability = clazz.getConstructor().newInstance().init(args);
            ability.setName(name);
            ability.init(args);
            if (ability.singleton()) AbilityManager.singltons.put(name, ability);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return ability;
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
