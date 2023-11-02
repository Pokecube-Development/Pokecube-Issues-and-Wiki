package pokecube.api.data.pokedex.conditions;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import pokecube.api.data.pokedex.EvolutionDataLoader;
import thut.lib.CompatParser.ClassFinder;

public class PokemobConditionLoader
{

    private static Set<Package> packages = Sets.newHashSet();

    public static void registerMatcherPackage(Package pack)
    {
        packages.add(pack);
    }

    static
    {
        registerMatcherPackage(PokemobConditionLoader.class.getPackage());
    }

    @SuppressWarnings("unchecked")
    public static void init()
    {
        // Initialise this here to ensure it loads
        EvolutionDataLoader.INSTANCE.getKey();
        
        List<Class<?>> foundClasses = Lists.newArrayList();

        Type ANNOTE = Type.getType("Lpokecube/api/data/pokedex/conditions/Condition;");
        BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
            for (final AnnotationData a : file.getScanResult().getAnnotations())
                if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(ANNOTE)) return true;
            return false;
        };

        for (final Package pack : PokemobConditionLoader.packages)
        {
            if (pack == null) continue;
            try
            {
                foundClasses.addAll(ClassFinder.find(pack.getName(), validClass));
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            for (final Class<?> candidateClass : foundClasses)
            {
                // Needs annotation
                if (candidateClass.getAnnotations().length == 0) continue;
                final Condition details = candidateClass.getAnnotation(Condition.class);
                if (details == null) continue;
                String key = details.name();
                PokemobCondition.CONDITIONS.put(key, (Class<? extends PokemobCondition>) candidateClass);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
