package thut.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class ResourceHelper
{
    public static Supplier<ResourceManager> RESOURCE_SOURCE = () -> ServerLifecycleHooks.getCurrentServer()
            .getResourceManager();
    public static Supplier<ResourceManager> RESOURCE_FALLBACK = () -> ServerLifecycleHooks.getCurrentServer()
            .getResourceManager();

    public static BufferedReader getReader(ResourceLocation l)
    {
        return getReader(l, RESOURCE_SOURCE.get());
    }

    @Nullable
    public static BufferedReader getReader(ResourceLocation l, ResourceManager source)
    {
        try
        {
            return source.openAsReader(l);
        }
        catch (Exception e)
        {
            try
            {
                var testSrc = RESOURCE_SOURCE.get();
                var testFb = RESOURCE_FALLBACK.get();
                if (source == testSrc && testSrc != testFb) return getReader(l, testFb);
            }
            catch (Exception ex)
            {
                return null;
            }
            return null;
        }
    }

    public static InputStream getStream(ResourceLocation l)
    {
        return getStream(l, RESOURCE_SOURCE.get());
    }

    @Nullable
    public static InputStream getStream(ResourceLocation l, ResourceManager source)
    {
        try
        {
            return source.open(l);
        }
        catch (Exception e)
        {
            try
            {
                var testSrc = RESOURCE_SOURCE.get();
                var testFb = RESOURCE_FALLBACK.get();
                if (source == testSrc && testSrc != testFb) return getStream(l, testFb);
            }
            catch (Exception ex)
            {
                return null;
            }
            return null;
        }
    }

    public static boolean exists(ResourceLocation l)
    {
        return exists(l, RESOURCE_SOURCE.get());
    }

    public static boolean exists(ResourceLocation l, ResourceManager source)
    {
        try
        {
            source.getResourceOrThrow(l);
            return true;
        }
        catch (Exception e)
        {
            try
            {
                var testSrc = RESOURCE_SOURCE.get();
                var testFb = RESOURCE_FALLBACK.get();
                if (source == testSrc && testSrc != testFb) return exists(l, testFb);
            }
            catch (Exception ex)
            {
                return false;
            }
            return false;
        }
    }

    public static InputStream getStream(Resource r)
    {
        try
        {
            return r.open();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public static BufferedReader getReader(Resource r)
    {
        try
        {
            return r.openAsReader();
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
