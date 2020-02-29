package thut.lib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CompatParser
{

    public static class ClassFinder
    {

        private static final char DOT = '.';

        private static final char SLASH = '/';

        private static final String CLASS_SUFFIX = ".class";

        private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        public static List<Class<?>> find(final String scannedPackage) throws UnsupportedEncodingException,
                URISyntaxException
        {
            final String scannedPath = scannedPackage.replace(ClassFinder.DOT, ClassFinder.SLASH);
            final URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) throw new IllegalArgumentException(String.format(ClassFinder.BAD_PACKAGE_ERROR,
                    scannedPath, scannedPackage));
            String urlString = scannedUrl.toString();
            urlString = urlString.replaceFirst("jar:", "");
            urlString = urlString.replace("!" + scannedPath, "");
            final URI uri = new URI(urlString);
            final File scannedDir = new File(uri.getPath());

            final Set<Class<?>> classes = Sets.newHashSet();

            classes.addAll(ClassFinder.findInFolder(new File("./mods/"), scannedPackage));

            if (scannedDir.exists()) for (final File file : scannedDir.listFiles())
                classes.addAll(ClassFinder.findInFolder(file, scannedPackage));
            return Lists.newArrayList(classes);
        }

        private static List<Class<?>> findInFolder(File file, final String scannedPackage)
        {
            final List<Class<?>> classes = new ArrayList<>();
            // DOLATER maybe cache?
            if (file.toString().endsWith(".jar")) try
            {
                String name = file.toString();
                final String pack = scannedPackage.replace(ClassFinder.DOT, ClassFinder.SLASH) + ClassFinder.SLASH;
                name = name.replace("file:", "");
                name = name.replaceAll("(.jar)(.*)", ".jar");
                file = new File(name);
                final ZipFile zip = new ZipFile(file);
                final Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements())
                {
                    final ZipEntry entry = entries.nextElement();
                    final String s = entry.getName();
                    if (s.startsWith(pack) && s.endsWith(ClassFinder.CLASS_SUFFIX)) try
                    {
                        classes.add(Class.forName(s.replace(ClassFinder.CLASS_SUFFIX, "").replace(ClassFinder.SLASH,
                                ClassFinder.DOT)));
                    }
                    catch (final Throwable ignore)
                    {
                    }
                }
                zip.close();
            }
            catch (final Throwable e)
            {
                e.printStackTrace();
            }
            else
            {
                String resource = file.toString().replaceAll("\\" + System.getProperty("file.separator"), ".");
                if (resource.indexOf(scannedPackage) != -1) resource = resource.substring(resource.indexOf(
                        scannedPackage), resource.length());
                if (file.isDirectory()) for (final File child : file.listFiles())
                    classes.addAll(ClassFinder.findInFolder(child, scannedPackage));
                else if (resource.endsWith(ClassFinder.CLASS_SUFFIX))
                {
                    final int endIndex = resource.length() - ClassFinder.CLASS_SUFFIX.length();
                    final String className = resource.substring(0, endIndex);
                    try
                    {
                        classes.add(Class.forName(className));
                    }
                    catch (final Throwable ignore)
                    {
                    }
                }
            }
            return classes;
        }

    }

    public static void findClasses(final String classPackage,
            final Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods)
    {
        List<Class<?>> foundClasses;
        try
        {
            foundClasses = ClassFinder.find(classPackage);
            for (final Class<?> c : foundClasses)
                try
                {
                    CompatClass comp = null;
                    for (final java.lang.reflect.Method m : c.getMethods())
                        if ((comp = m.getAnnotation(CompatClass.class)) != null) initMethods.get(comp.phase()).add(m);
                }
                catch (final Throwable e)
                {
                }
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
    }
}
