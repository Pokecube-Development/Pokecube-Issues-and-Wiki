package pokecube.core.database.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.lib.ResourceHelper;

public class PackFinder implements RepositorySource
{
    public static long time_listing = 0;
    public static long time_getting_1 = 0;
    public static long time_getting_2 = 0;

    // TODO: Check this
    static final PackSource DECORATOR;
    static
    {
        DECORATOR = PackSource.create(component_in -> {
            Component component = Component.translatable("pack.source.pokecube.data");
            return Component.translatable("pack.nameAndSource", component_in, component).withStyle(ChatFormatting.GRAY);
        }, true);
    }


    public static Map<ResourceLocation, Resource> getJsonResources(final String path)
    {
        return PackFinder.getResources(path, s -> s.endsWith(".json"));
    }

    public static Map<ResourceLocation, Resource> getResources(String path, final Predicate<String> match)
    {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        long start = System.nanoTime();
        Map<ResourceLocation, Resource> ret =  Database.resourceManager.listResources(path, p->match.test(p.toString()));
        long end = System.nanoTime();
        time_listing += (end - start);
        return ret;
    }

    public static Map<ResourceLocation, List<Resource>> getAllJsonResources(final String path)
    {
        return PackFinder.getAllResources(path, s -> s.endsWith(".json"));
    }

    public static Map<ResourceLocation, List<Resource>> getAllResources(String path, final Predicate<String> match)
    {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        long start = System.nanoTime();
        Map<ResourceLocation, List<Resource>> ret =  Database.resourceManager.listResourceStacks(path, p->match.test(p.toString()));
        long end = System.nanoTime();
        time_listing += (end - start);
        return ret;
    }

    @Nullable
    public static InputStream getStream(ResourceLocation l)
    {
        if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        InputStream ret = ResourceHelper.getStream(l, Database.resourceManager);
        long end = System.nanoTime();
        time_getting_1 += (end - start);

        return ret;
    }

    @Nullable
    public static BufferedReader getReader(ResourceLocation l)
    {
        if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        BufferedReader ret = ResourceHelper.getReader(l, Database.resourceManager);
        long end = System.nanoTime();
        time_getting_1 += (end - start);

        return ret;
    }

    public static List<Resource> getResources(ResourceLocation l) throws IOException
    {
        if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        List<Resource> ret = Database.resourceManager.getResourceStack(l);
        long end = System.nanoTime();
        time_getting_2 += (end - start);

        return ret;
    }

//    TODO: Fix this
    public static final PackFinder DEFAULT_FINDER = new PackFinder(/*
        (name, component, bool, supplier, metadata, position, source, hidden) ->
        {
            return Pack.create(name, component, bool, supplier, metadata, PackType.SERVER_DATA, Pack.Position.TOP, source, hidden);
//            return new Pack(name, bool, supplier, metadata, PackType.SERVER_DATA, Pack.Position.TOP, source, hidden);
        }*/);

    public final List<PackResources> allPacks = Lists.newArrayList();
    public final List<PackResources> folderPacks = Lists.newArrayList();

    private final FolderRepositorySource folderFinder_old;
    private final FolderRepositorySource folderFinder_new;

    public PackFinder()
    {
        Path folder = FMLPaths.GAMEDIR.get().resolve("resourcepacks");
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Adding data folder: {}", folder);
        this.folderFinder_old = new FolderRepositorySource(folder, PackType.SERVER_DATA, PackFinder.DECORATOR);
        folder = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks");
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Adding data folder: {}", folder);
        this.folderFinder_new = new FolderRepositorySource(folder, PackType.SERVER_DATA, PackFinder.DECORATOR);
        this.init();
    }

    public void init()
    {
        try
        {
            final List<PathPackResources> packs = Lists.newArrayList();
            ModList.get().getModFiles().stream().forEach(mf -> packs.add(ResourcePackLoader.createPackForMod(mf)));
            this.allPacks.addAll(packs);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }

        final Map<String, Pack> map = Maps.newHashMap();
        try
        {
            this.folderFinder_old.loadPacks(a -> map.put(a.getId(), a));
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.fatal("Error checking resourcepacks for data!", e);
        }
        try
        {
            this.folderFinder_new.loadPacks(a -> map.put(a.getId(), a));
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.fatal("Error checking config/pokecube/datapacks for data!", e);
        }
        for (final Pack info : map.values())
        {
            final PackResources pack = info.open();
            if (pack != null)
            {
                this.allPacks.add(pack);
                this.folderPacks.add(pack);
            }
            else PokecubeAPI.LOGGER.error("No Pack found for " + info);
        }
    }

    @Override
    public void loadPacks(final Consumer<Pack> infoConsumer)
    {
        this.folderFinder_new.loadPacks(infoConsumer);
    }

}
