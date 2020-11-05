package pokecube.legends.conditions;

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
import pokecube.legends.handlers.GeneProtector;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.spawns.LegendarySpawn;
import thut.lib.CompatParser.ClassFinder;

public class LegendaryConditions
{
    public static List<PokedexEntry> entries = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public void init()
    {
        // Registring Event Lengendary Spawns
        new LegendarySpawn("ho-oh", ItemInit.LEGENDARYORB, BlockInit.LEGENDARY_SPAWN);
        new LegendarySpawn("lugia", ItemInit.OCEANORB, BlockInit.LEGENDARY_SPAWN);
        new LegendarySpawn("celebi", ItemInit.GREENORB, BlockInit.LEGENDARY_SPAWN);

        new LegendarySpawn("registeel", ItemInit.STEELCORE, BlockInit.REGISTEEL_CORE);
        new LegendarySpawn("regirock", ItemInit.ROCKCORE, BlockInit.REGIROCK_CORE);
        new LegendarySpawn("regice", ItemInit.ICECORE, BlockInit.REGICE_CORE);      
        new LegendarySpawn("regidrago", ItemInit.DRAGOCORE, BlockInit.REGIDRAGO_CORE);
        new LegendarySpawn("regieleki", ItemInit.THUNDERCORE, BlockInit.REGIELEKI_CORE);
        
        new LegendarySpawn("regigigas", ItemInit.REGIS_ORB, BlockInit.REGIGIGA_CORE);

        new LegendarySpawn("groudon", ItemInit.REDORB, BlockInit.LEGENDARY_SPAWN);
        new LegendarySpawn("kyogre", ItemInit.BLUEORB, BlockInit.LEGENDARY_SPAWN);
        new LegendarySpawn("rayquaza", ItemInit.GREENORB, BlockInit.LEGENDARY_SPAWN);

        new LegendarySpawn("arceus", ItemInit.AZURE_FLUTE, BlockInit.TIMESPACE_CORE);
        new LegendarySpawn("palkia", ItemInit.LUSTROUSORB, BlockInit.TIMESPACE_CORE);
        new LegendarySpawn("dialga", ItemInit.ADAMANTORB, BlockInit.TIMESPACE_CORE);
        new LegendarySpawn("reshiram", ItemInit.LIGHTSTONE, BlockInit.TIMESPACE_CORE);
        new LegendarySpawn("zekrom", ItemInit.DARKSTONE, BlockInit.TIMESPACE_CORE);

        new LegendarySpawn("heatran", ItemInit.MAGMA_CORE, BlockInit.HEATRAN_BLOCK);
        new LegendarySpawn("keldeo", ItemInit.RAINBOW_SWORD, BlockInit.KELDEO_CORE);

        new LegendarySpawn("landorusincarnate", ItemInit.ORANGE_RUNE, BlockInit.NATURE_CORE);
        new LegendarySpawn("thundurusincarnate", ItemInit.BLUE_RUNE, BlockInit.NATURE_CORE);
        new LegendarySpawn("tornadusincarnate", ItemInit.GREEN_RUNE, BlockInit.NATURE_CORE);

        new LegendarySpawn("victini", ItemInit.EMBLEM, BlockInit.VICTINI_CORE);

        new LegendarySpawn("xerneas", ItemInit.LIFEORB, BlockInit.XERNEAS_CORE);
        new LegendarySpawn("yveltal", ItemInit.DESTRUCTORB, BlockInit.YVELTAL_CORE);

        new LegendarySpawn("zacian", ItemInit.RSWORD, BlockInit.LEGENDARY_SPAWN);
        new LegendarySpawn("zamazenta", ItemInit.RSHIELD, BlockInit.LEGENDARY_SPAWN);
        
        new LegendarySpawn("glastrier", ItemInit.ICE_CARROT, BlockInit.TROUGH_BLOCK);
        new LegendarySpawn("spectrier", ItemInit.SHADOW_CARROT, BlockInit.TROUGH_BLOCK);

        // Register the thng that prevents genetic modification of protected
        // mobs
        MinecraftForge.EVENT_BUS.register(new GeneProtector());
        PokecubeCore.POKEMOB_BUS.register(new GeneProtector());
        MinecraftForge.EVENT_BUS.register(LegendarySpawn.class);

        List<Class<?>> foundClasses;
        final List<Class<? extends Condition>> conditionclasses = Lists.newArrayList();
        try
        {
            foundClasses = ClassFinder.find(Condition.class.getPackage().getName());
            int num = 0;
            for (final Class<?> candidateClass : foundClasses)
                if (Condition.class.isAssignableFrom(candidateClass) && candidateClass != Condition.class)
                {
                    conditionclasses.add((Class<? extends Condition>) candidateClass);
                    num++;
                }
            if (PokecubeMod.debug) PokecubeMod.LOGGER.info("Detected " + num + " Legendary Conditions.");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        int num = 0;
        for (final Class<? extends Condition> c : conditionclasses)
            try
            {
                final Condition cond = c.newInstance();
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
