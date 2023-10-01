package pokecube.api.data.spawns.matchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import thut.lib.CompatParser.ClassFinder;

public class MatcherLoaders
{
    private static Set<Package> packages = Sets.newHashSet();
    public static Map<String, Class<? extends MatchChecker>> matchClasses = new HashMap<>();

    public static void registerMatcherPackage(Package pack)
    {
        packages.add(pack);
    }

    static
    {
        registerMatcherPackage(MatcherLoaders.class.getPackage());
    }

    @SuppressWarnings("unchecked")
    public static void init()
    {
        List<Class<?>> foundClasses = Lists.newArrayList();

        Type ANNOTE = Type.getType("Lpokecube/api/data/spawns/matchers/MatcherFunction;");
        BiFunction<ModFile, String, Boolean> validClass = (file, name) -> {
            for (final AnnotationData a : file.getScanResult().getAnnotations())
                if (name.equals(a.clazz().getClassName()) && a.annotationType().equals(ANNOTE)) return true;
            return false;
        };

        for (final Package pack : MatcherLoaders.packages)
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
                final MatcherFunction details = candidateClass.getAnnotation(MatcherFunction.class);
                if (details == null) continue;
                String key = details.name();
                matchClasses.put(key, (Class<? extends MatchChecker>) candidateClass);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
