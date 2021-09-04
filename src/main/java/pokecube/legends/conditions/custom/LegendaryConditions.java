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
//        new LegendarySpawn("ho-oh", ItemInit.RAINBOW_ORB, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("lugia", ItemInit.OCEAN_ORB, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("celebi", ItemInit.GREEN_ORB, BlockInit.LEGENDARY_SPAWN);

        new LegendarySpawn("registeel", ItemInit.STEEL_CORE, BlockInit.REGISTEEL_CORE);
        new LegendarySpawn("regirock", ItemInit.ROCK_CORE, BlockInit.REGIROCK_CORE);
        new LegendarySpawn("regice", ItemInit.ICE_CORE, BlockInit.REGICE_CORE);
        new LegendarySpawn("regidrago", ItemInit.DRAGO_CORE, BlockInit.REGIDRAGO_CORE);
        new LegendarySpawn("regieleki", ItemInit.THUNDER_CORE, BlockInit.REGIELEKI_CORE);

        new LegendarySpawn("regigigas", ItemInit.REGIS_ORB, BlockInit.REGIGIGA_CORE);

//        new LegendarySpawn("groudon", ItemInit.RED_ORB, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("kyogre", ItemInit.BLUE_ORB, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("rayquaza", ItemInit.ANCIENT_STONE, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("arceus", ItemInit.AZURE_FLUTE, BlockInit.TIMESPACE_CORE);
//        new LegendarySpawn("palkia", ItemInit.LUSTROUS_ORB, BlockInit.TIMESPACE_CORE);
//        new LegendarySpawn("dialga", ItemInit.ADAMANT_ORB, BlockInit.TIMESPACE_CORE);

//        new LegendarySpawn("reshiram", ItemInit.LIGHT_STONE, BlockInit.TAO_BLOCK);
//        new LegendarySpawn("zekrom", ItemInit.DARK_STONE, BlockInit.TAO_BLOCK);

//        new LegendarySpawn("heatran", ItemInit.MAGMA_CORE, BlockInit.HEATRAN_BLOCK);
//        new LegendarySpawn("keldeo", ItemInit.KELDEO_SWORD, BlockInit.KELDEO_CORE);

//        new LegendarySpawn("landorusincarnate", ItemInit.ORANGE_RUNE, BlockInit.NATURE_CORE);
//        new LegendarySpawn("thundurusincarnate", ItemInit.BLUE_RUNE, BlockInit.NATURE_CORE);
//        new LegendarySpawn("tornadusincarnate", ItemInit.GREEN_RUNE, BlockInit.NATURE_CORE);

//        new LegendarySpawn("victini", ItemInit.EMBLEM, BlockInit.VICTINI_CORE);

//        new LegendarySpawn("xerneas", ItemInit.LIFE_ORB, BlockInit.XERNEAS_CORE);
//        new LegendarySpawn("yveltal", ItemInit.DESTRUCT_ORB, BlockInit.YVELTAL_CORE);

//        new LegendarySpawn("tapu_koko", ItemInit.KOKO_ORB, BlockInit.TAPU_KOKO_CORE);
//        new LegendarySpawn("tapu_bulu", ItemInit.BULU_ORB, BlockInit.TAPU_BULU_CORE);
//        new LegendarySpawn("tapu_fini", ItemInit.FINI_ORB, BlockInit.TAPU_FINI_CORE);
//        new LegendarySpawn("tapu_lele", ItemInit.LELE_ORB, BlockInit.TAPU_LELE_CORE);

//        new LegendarySpawn("zacian", ItemInit.RSWORD, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("zamazenta", ItemInit.RSHIELD, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("glastrier", ItemInit.ICE_CARROT, BlockInit.TROUGH_BLOCK);
//        new LegendarySpawn("spectrier", ItemInit.SHADOW_CARROT, BlockInit.TROUGH_BLOCK);

        // Legendary spawn alternative
//        new LegendarySpawn("articuno", ItemInit.ICE_WING, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("articunogalar", ItemInit.ICE_DARK_WING, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("zapdos", ItemInit.ELECTRIC_WING, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("zapdosgalar", ItemInit.STATIC_WING, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("moltres", ItemInit.FIRE_WING, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("moltresgalar", ItemInit.DARK_FIRE_WING, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("entei", ItemInit.FLAME_GEM, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("raikou", ItemInit.THUNDER_GEM, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("suicune", ItemInit.WATER_GEM, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("cobalion", ItemInit.COBALION_SWORD, BlockInit.KELDEO_CORE);
//        new LegendarySpawn("terrakion", ItemInit.TERRAKION_SWORD, BlockInit.KELDEO_CORE);
//        new LegendarySpawn("virizion", ItemInit.VIRIZION_SWORD, BlockInit.KELDEO_CORE);

//        new LegendarySpawn("deoxys", ItemInit.METEOR_SHARD, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("jirachi", ItemInit.STAR_CORE, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("magearna", ItemInit.SOUL_HEART, BlockInit.MAGEARNA_BLOCK);
//        new LegendarySpawn("zygarde10", ItemInit.ZYGARDE_CUBE, BlockInit.XERNEAS_CORE);
//        new LegendarySpawn("zygarde10", ItemInit.ZYGARDE_CUBE, BlockInit.YVELTAL_CORE);

//        new LegendarySpawn("cosmog", ItemInit.COSMIC_ORB, BlockInit.TIMESPACE_CORE);
//        new LegendarySpawn("necrozma", ItemInit.LIGHTING_CRYSTAL, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("kubfu", ItemInit.GRAY_SCARF, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("calyrex", ItemInit.WOODEN_CROWN, BlockInit.TROUGH_BLOCK);

//        new LegendarySpawn("latios", ItemInit.SOUL_DEW, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("latias", ItemInit.SOUL_DEW, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("darkrai", ItemInit.NIGHTMARE_BOOK, BlockInit.YVELTAL_CORE);
//        new LegendarySpawn("cresselia", ItemInit.LUNAR_WING, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("azelf", ItemInit.AZELF_GEM, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("mesprit", ItemInit.MESPRIT_GEM, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("uxie", ItemInit.UXIE_GEM, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("diancie", ItemInit.DIAMOND_GEM, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("manaphy", ItemInit.MANAPHY_NECKLACE, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("kyurem", ItemInit.KYUREM_CORE, BlockInit.LEGENDARY_SPAWN);
//        new LegendarySpawn("hoopaconfined", ItemInit.PRISION_BOTTLE, BlockInit.LEGENDARY_SPAWN);

//        new LegendarySpawn("volcanion", ItemInit.STEAM_CORE, BlockInit.MAGEARNA_BLOCK);
//        new LegendarySpawn("meloettaaria", ItemInit.MELOETTA_OCARINA, BlockInit.NATURE_CORE);


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
