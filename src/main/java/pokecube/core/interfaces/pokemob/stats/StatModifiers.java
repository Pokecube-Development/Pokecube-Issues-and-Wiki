package pokecube.core.interfaces.pokemob.stats;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.pokemob.IHasStats;
import pokecube.core.utils.PokeType;

public class StatModifiers
{

    public static final String                                  DEFAULTMODIFIERS = "default";
    public static Map<String, Class<? extends IStatsModifiers>> modifierClasses  = Maps.newHashMap();

    static
    {
        StatModifiers.modifierClasses.put(StatModifiers.DEFAULTMODIFIERS, DefaultModifiers.class);
    }

    public static void registerModifier(String name, Class<? extends IStatsModifiers> modclass)
    {
        if (!StatModifiers.modifierClasses.containsKey(name)) StatModifiers.modifierClasses.put(name, modclass);
        else throw new IllegalArgumentException(name + " is already registered as a modifier.");
    }

    final Map<String, IStatsModifiers> modifiers       = Maps.newHashMap();
    public List<IStatsModifiers>       sortedModifiers = Lists.newArrayList();
    public Map<String, Integer>        indecies        = Maps.newHashMap();
    /** This are types which may be modified via abilities or moves. */
    public PokeType                    type1, type2;
    DefaultModifiers                   defaultmods;

    public StatModifiers()
    {
        for (final String s : StatModifiers.modifierClasses.keySet())
            try
            {
                this.modifiers.put(s, StatModifiers.modifierClasses.get(s).newInstance());
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        this.defaultmods = this.getModifiers(StatModifiers.DEFAULTMODIFIERS, DefaultModifiers.class);
        this.sortedModifiers.addAll(this.modifiers.values());
        Collections.sort(this.sortedModifiers, (o1, o2) ->
        {
            int comp = o1.getPriority() - o2.getPriority();
            if (comp == 0) comp = o1.getClass().getName().compareTo(o2.getClass().getName());
            return comp;
        });
        outer:
        for (int i = 0; i < this.sortedModifiers.size(); i++)
            for (final String key : this.modifiers.keySet())
                if (this.modifiers.get(key) == this.sortedModifiers.get(i))
                {
                    this.indecies.put(key, i);
                    continue outer;
                }
    }

    public DefaultModifiers getDefaultMods()
    {
        return this.defaultmods;
    }

    public IStatsModifiers getModifiers(String name)
    {
        return this.modifiers.get(name);
    }

    public <T extends IStatsModifiers> T getModifiers(String name, Class<T> type)
    {
        return type.cast(this.modifiers.get(name));
    }

    public float getStat(IHasStats pokemob, Stats stat, boolean modified)
    {
        if (modified && stat == Stats.HP) return pokemob.getHealth();
        final int index = stat.ordinal();
        byte nature = 0;
        if (index < 6) nature = pokemob.getNature().stats[index];
        final float natureMod = (nature * 10f + 100) / 100f;
        final int baseStat = pokemob.getBaseStat(stat);
        float actualStat = 1;
        if (index < 6)
        {
            final int IV = pokemob.getIVs()[stat.ordinal()];
            final int EV = pokemob.getEVs()[stat.ordinal()] - Byte.MIN_VALUE;
            final int level = pokemob.getLevel();
            if (stat == Stats.HP)
            {
                if (baseStat != 1) actualStat = level + 10 + (2 * baseStat + IV + EV / 4) * level / 100;
                else actualStat = 1;
            }
            else
            {
                actualStat = 5 + level * (2 * baseStat + IV + EV / 4) / 100;
                actualStat *= natureMod;
            }
        }
        if (modified) for (final IStatsModifiers mods : this.sortedModifiers)
            if (mods.isFlat()) actualStat += mods.getModifier(stat);
            else actualStat *= mods.getModifier(stat);
        return actualStat;
    }

    public void outOfCombatReset()
    {
        this.defaultmods.reset();
        for (final IStatsModifiers mods : this.sortedModifiers)
            if (!mods.persistant()) mods.reset();
    }
}
