package pokecube.legends.conditions.custom;

import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.stats.ISpecialCaptureCondition;
import pokecube.api.stats.ISpecialSpawnCondition;
import pokecube.api.stats.SpecialCaseRegister;
import pokecube.core.PokecubeCore;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.data.ConditionLoader;
import pokecube.legends.handlers.GeneProtector;
import pokecube.legends.spawns.LegendarySpawn;
import thut.core.common.ThutCore;
import thut.lib.CompatParser.ClassFinder;

public class LegendaryConditions
{
    public static final ConditionLoader CONDITIONS = new ConditionLoader("database/legend_conditions/");

    public static List<PokedexEntry> entries = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public void init()
    {
        // Registring Event Lengendary Spawns
        // Register the thng that prevents genetic modification of protected
        // mobs
        ThutCore.FORGE_BUS.register(new GeneProtector());
        PokecubeAPI.POKEMOB_BUS.register(new GeneProtector());
        ThutCore.FORGE_BUS.register(LegendarySpawn.class);

        List<Class<?>> foundClasses;
        final List<Class<? extends AbstractCondition>> conditionclasses = Lists.newArrayList();
        try
        {
            foundClasses = ClassFinder.find(LegendaryConditions.class.getPackage().getName());
            int num = 0;
            for (final Class<?> candidateClass : foundClasses)
            {
                if (Modifier.isAbstract(candidateClass.getModifiers())) continue;
                if (AbstractCondition.class.isAssignableFrom(candidateClass))
                {
                    conditionclasses.add((Class<? extends AbstractCondition>) candidateClass);
                    num++;
                }
            }
            if (PokecubeCore.getConfig().debug_misc)
                PokecubeAPI.logInfo("Detected " + num + " Legendary Conditions.");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        int num = 0;
        for (final Class<? extends AbstractCondition> c : conditionclasses) try
        {
            final AbstractCondition cond = c.getConstructor().newInstance();
            final PokedexEntry e = cond.getEntry();
            if (Pokedex.getInstance().isRegistered(e))
            {
                SpecialCaseRegister.register(e.getName(), (ISpecialCaptureCondition) cond);
                SpecialCaseRegister.register(e.getName(), (ISpecialSpawnCondition) cond);
                num++;
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        PokecubeAPI.logInfo("Registered " + num + " Legendary Conditions.");
    }
}
