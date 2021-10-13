package thut.lib;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;

public class CompatParser
{

    public static class ClassFinder
    {
        public static List<Class<?>> find(final String packageName) throws IOException
        {
            final Set<Class<?>> classes = ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream().filter(
                    clazz -> clazz.getPackageName().startsWith(packageName)).map(clazz -> clazz.load()).collect(
                            Collectors.toSet());
            final List<Class<?>> ret = Lists.newArrayList();
            classes.forEach(c ->
            {
                try
                {
                    ret.add(ClassFinder.washClass(c));
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            });
            return ret;
        }

        @SuppressWarnings("unchecked")
        public static <T> Class<T> washClass(final Class<T> input) throws Exception
        {
            return (Class<T>) ClassFinder.class.getClassLoader().loadClass(input.getName());
        }
    }
}
