package thut.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class ResourceHelper
{
    @Nullable
    public static BufferedReader getReader(ResourceLocation l, ResourceManager source)
    {
        try
        {
            return source.openAsReader(l);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @Nullable
    public static InputStream getStream(ResourceLocation l, ResourceManager source)
    {
        try
        {
            return source.open(l);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @Nullable
    public static Resource getResource(ResourceLocation l, ResourceManager source)
    {
        try
        {
            return source.getResourceOrThrow(l);
        }
        catch (IOException e)
        {
            return null;
        }
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
            return false;
        }
    }
}
