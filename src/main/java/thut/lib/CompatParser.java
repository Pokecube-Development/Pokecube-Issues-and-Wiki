package thut.lib;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.ModFileScanData.ClassData;
import thut.core.common.ThutCore;

public class CompatParser
{
    public static class ClassFinder
    {
        private static final Map<String, ModFile> OPTIONS = Maps.newConcurrentMap();

        private static void checkOptions()
        {
            if (!ClassFinder.OPTIONS.isEmpty()) return;

            synchronized (ClassFinder.OPTIONS)
            {
                FMLLoader.getLoadingModList().getMods().forEach(i ->
                {
                    final Set<ClassData> classes = i.getOwningFile().getFile().getScanResult().getClasses();
                    classes.forEach(c ->
                    {
                        ClassFinder.OPTIONS.put(c.clazz().getClassName(), i.getOwningFile().getFile());
                    });
                });
            }
        }

        public static List<Class<?>> find(final String packageName) throws IOException
        {
            return ClassFinder.find(packageName, (i, n) -> true);
        }

        public static List<Class<?>> find(final String packageName, final BiFunction<ModFile, String, Boolean> valid)
                throws IOException
        {
            ClassFinder.checkOptions();
            final List<Class<?>> ret = Lists.newArrayList();
            ClassFinder.OPTIONS.keySet().forEach(c ->
            {
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
