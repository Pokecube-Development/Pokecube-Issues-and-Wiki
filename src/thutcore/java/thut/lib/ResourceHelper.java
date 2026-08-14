package thut.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class ResourceHelper
{
    public static BufferedReader getReader(ResourceLocation l)
    {
        return getReader(l, Minecraft.getInstance().getResourceManager());
    }

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

    public static InputStream getStream(ResourceLocation l)
    {
        return getStream(l, Minecraft.getInstance().getResourceManager());
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

    public static boolean exists(ResourceLocation l)
    {
        return exists(l, Minecraft.getInstance().getResourceManager());
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
