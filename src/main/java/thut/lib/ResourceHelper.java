package thut.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
            return new BufferedReader(
                    new InputStreamReader(source.getResource(l).getInputStream(), StandardCharsets.UTF_8));
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
            return source.getResource(l).getInputStream();
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
            return source.getResource(l);
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
            Resource res = source.getResource(l);
            res.close();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static InputStream getStream(Resource r)
    {
        return r.getInputStream();
    }

    public static BufferedReader getReader(Resource r)
    {
        return new BufferedReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8));
    }
}
