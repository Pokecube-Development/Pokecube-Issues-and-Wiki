package pokecube.legends.conditions.custom;

import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.data.ConditionLoader;
import pokecube.legends.handlers.GeneProtector;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.spawns.LegendarySpawn;
import thut.lib.CompatParser.ClassFinder;

public class LegendaryConditions
{
    public static final ConditionLoader CONDITIONS = new ConditionLoader("database/legend_conditions/");

    public static List<PokedexEntry> entries = Lists.newArrayList();

    @SuppressWarnings("unchecked")
	public void init()
    {
        // Registring Event Lengendary Spawns
        new LegendarySpawn("registeel", ItemInit.STEEL_CORE, BlockInit.REGISTEEL_CORE);
        new LegendarySpawn("regirock", ItemInit.ROCK_CORE, BlockInit.REGIROCK_CORE);
        new LegendarySpawn("regice", ItemInit.ICE_CORE, BlockInit.REGICE_CORE);
        new LegendarySpawn("regidrago", ItemInit.DRAGO_CORE, BlockInit.REGIDRAGO_CORE);
        new LegendarySpawn("regieleki", ItemInit.THUNDER_CORE, BlockInit.REGIELEKI_CORE);

        new LegendarySpawn("regigigas", ItemInit.REGIS_ORB, BlockInit.REGIGIGA_CORE);

        // Register the thng that prevents genetic modification of protected
        // mobs
        MinecraftForge.EVENT_BUS.register(new GeneProtector());
        PokecubeCore.POKEMOB_BUS.register(new GeneProtector());
        MinecraftForge.EVENT_BUS.register(LegendarySpawn.class);

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
            if (PokecubeMod.debug) PokecubeMod.LOGGER.info("Detected " + num + " Legendary Conditions.");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        int num = 0;
        for (final Class<? extends AbstractCondition> c : conditionclasses)
            try
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
        PokecubeMod.LOGGER.info("Registered " + num + " Legendary Conditions.");
    }
}
