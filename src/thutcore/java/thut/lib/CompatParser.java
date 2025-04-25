package thut.lib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.ModFileScanData.ClassData;
import net.neoforged.neoforgespi.locating.IModFile;
import thut.core.common.ThutCore;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class CompatParser
{
    public static class ClassFinder
    {
        private static final Map<String, IModFile> OPTIONS = Maps.newConcurrentMap();

        private static void checkOptions()
        {
            if (!ClassFinder.OPTIONS.isEmpty()) return;

            synchronized (ClassFinder.OPTIONS)
            {
                FMLLoader.getLoadingModList().getMods().forEach(i -> {
                    final Set<ClassData> classes = i.getOwningFile().getFile().getScanResult().getClasses();
                    classes.forEach(c -> {
                        ClassFinder.OPTIONS.put(c.clazz().getClassName(), i.getOwningFile().getFile());
                    });
                });
            }
        }

        public static List<Class<?>> find(final String packageName) throws IOException
        {
            return ClassFinder.find(packageName, (i, n) -> true);
        }

        public static List<Class<?>> find(final String packageName, final BiFunction<IModFile, String, Boolean> valid)
                throws IOException
        {
            ClassFinder.checkOptions();
            final List<Class<?>> ret = Lists.newArrayList();
            ClassFinder.OPTIONS.keySet().forEach(c -> {
                if (!c.startsWith(packageName)) return;
                if (!valid.apply(ClassFinder.OPTIONS.get(c), c)) return;
                try
                {
                    ret.add(ClassFinder.washClass(c));
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            });
            if (ThutCore.conf.debug_data)
                ThutCore.LOGGER.info("Found {} classes in package {}", ret.size(), packageName);
            return ret;
        }

        @SuppressWarnings("unchecked")
        public static <T> Class<T> washClass(final String input) throws Exception
        {
            return (Class<T>) ClassFinder.class.getClassLoader().loadClass(input);
        }
    }
}
