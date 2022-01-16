package pokecube.core.database.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.PackConstructor;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.PathResourcePack;
import net.minecraftforge.resource.ResourcePackLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;

public class PackFinder implements RepositorySource
{
    public static long time_listing = 0;
    public static long time_getting_1 = 0;
    public static long time_getting_2 = 0;

    static final PackSource DECORATOR = PackSource.decorating("pack.source.pokecube.data");

    public static Collection<ResourceLocation> getJsonResources(final String path)
    {
        return PackFinder.getResources(path, s -> s.endsWith(".json"));
    }

    public static Collection<ResourceLocation> getResources(String path, final Predicate<String> match)
    {
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

        long start = System.nanoTime();
        Collection<ResourceLocation> ret = Database.resourceManager.listResources(path, match);
        long end = System.nanoTime();
        time_listing += (end - start);

        return ret;
    }

    public static InputStream getStream(ResourceLocation l) throws IOException
    {
        if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        InputStream ret = Database.resourceManager.getResource(l).getInputStream();
        long end = System.nanoTime();
        time_getting_1 += (end - start);

        return ret;
    }

    public static List<Resource> getResources(ResourceLocation l) throws IOException
    {
        if (l.toString().contains("//")) l = new ResourceLocation(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        List<Resource> ret = Database.resourceManager.getResources(l);
        long end = System.nanoTime();
        time_getting_2 += (end - start);

        return ret;
    }

    public final List<PackResources> allPacks = Lists.newArrayList();
    public Set<PackResources> folderPacks = Sets.newHashSet();

    private final FolderRepositorySource folderFinder_old;
    private final FolderRepositorySource folderFinder_new;

    public PackFinder(final Pack.PackConstructor packInfoFactoryIn)
    {
        File folder = FMLPaths.GAMEDIR.get().resolve("resourcepacks").toFile();
        folder.mkdirs();
        PokecubeCore.LOGGER.debug("Adding data folder: {}", folder);
        this.folderFinder_old = new FolderRepositorySource(folder, PackFinder.DECORATOR);
        folder = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks").toFile();
        folder.mkdirs();
        PokecubeCore.LOGGER.debug("Adding data folder: {}", folder);
        this.folderFinder_new = new FolderRepositorySource(folder, PackFinder.DECORATOR);
        this.init(packInfoFactoryIn);
    }

    public void init(final Pack.PackConstructor packInfoFactoryIn)
    {
        try
        {
            final List<PathResourcePack> packs = Lists.newArrayList();
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
            this.folderFinder_old.loadPacks(a -> map.put(a.getId(), a), packInfoFactoryIn);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error checking resourcepacks for data!", e);
        }
        try
        {
            this.folderFinder_new.loadPacks(a -> map.put(a.getId(), a), packInfoFactoryIn);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error checking config/pokecube/datapacks for data!", e);
        }

        for (final Pack info : map.values())
        {
            final PackResources pack = info.open();
            if (pack != null)
            {
                this.allPacks.add(pack);
                this.folderPacks.add(pack);
            }
            else PokecubeCore.LOGGER.debug("No Pack found for " + info);
        }
    }

    @Override
    public void loadPacks(final Consumer<Pack> infoConsumer, final PackConstructor infoFactory)
    {
        throw new RuntimeException("Opps we, don't do this yet!");
    }

}
