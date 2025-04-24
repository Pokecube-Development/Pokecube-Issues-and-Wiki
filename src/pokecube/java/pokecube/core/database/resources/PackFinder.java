package pokecube.core.database.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.lib.ResourceHelper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class PackFinder implements RepositorySource
{
    public static long time_listing = 0;
    public static long time_getting_1 = 0;
    public static long time_getting_2 = 0;

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
        Map<ResourceLocation, Resource> ret = Database.resourceManager.listResources(path,
                p -> match.test(p.toString()));
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
        Map<ResourceLocation, List<Resource>> ret = Database.resourceManager.listResourceStacks(path,
                p -> match.test(p.toString()));
        long end = System.nanoTime();
        time_listing += (end - start);
        return ret;
    }

    @Nullable
    public static InputStream getStream(ResourceLocation l)
    {
        if (l.toString().contains("//")) l = ResourceLocation.parse(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        InputStream ret = ResourceHelper.getStream(l, Database.resourceManager);
        long end = System.nanoTime();
        time_getting_1 += (end - start);

        return ret;
    }

    @Nullable
    public static BufferedReader getReader(ResourceLocation l)
    {
        if (l.toString().contains("//")) l = ResourceLocation.parse(l.toString().replace("//", "/"));

        long start = System.nanoTime();
        BufferedReader ret = ResourceHelper.getReader(l, Database.resourceManager);
        long end = System.nanoTime();
        time_getting_1 += (end - start);

        return ret;
    }

    public static boolean PRE_REG_INIT = true;

    public static final PackFinder DEFAULT_FINDER = new PackFinder();

    public final List<Supplier<PackResources>> allPacks = Lists.newArrayList();
    public final List<Supplier<PackResources>> folderPacks = Lists.newArrayList();

    private final FolderRepositorySource folderFinder_old;
    private final FolderRepositorySource folderFinder_new;

    public PackFinder()
    {
        File folder = FMLPaths.GAMEDIR.get().resolve("resourcepacks").toFile();
        folder.mkdirs();
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Adding data folder: {}", folder);
        var validator = new DirectoryValidator(p -> true);
        this.folderFinder_old = new FolderRepositorySource(folder.toPath(), PackType.SERVER_DATA, PackFinder.DECORATOR,
                validator);
        folder = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks").toFile();
        folder.mkdirs();
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Adding data folder: {}", folder);
        this.folderFinder_new = new FolderRepositorySource(folder.toPath(), PackType.SERVER_DATA, PackFinder.DECORATOR,
                validator);
        this.init();
    }

    public void init()
    {
        try
        {
            PackRepository packs = new PackRepository();
            ResourcePackLoaderQuiet.populatePackRepository(packs, PackType.SERVER_DATA);
            ModList.get().forEachModFile(modfile -> {
                try
                {
                    modfile.getModInfos().forEach(info -> {
                        try
                        {
                            var modid = info.getModId();
                            if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("modinfo: " + modid);
                            this.allPacks.add(() -> {
                                var opt = ResourcePackLoader.getPackFor(modid);
                                if (opt.isEmpty())
                                {
                                    if (PokecubeCore.getConfig().debug_data)
                                        PokecubeAPI.logInfo("No data for " + modid);
                                    return null;
                                }
                                return opt.get().openPrimary(
                                        new PackLocationInfo("mod/" + modid, Component.empty(), PackSource.BUILT_IN,
                                                Optional.empty()));
                            });
                        }
                        catch (Exception e)
                        {
                            PokecubeAPI.LOGGER.error("Error scanning info {}", info, e);
                        }
                    });
                    if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Adding data packs: " + modfile);
                }
                catch (Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error scanning modfile {}", modfile, e);
                }
            });
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
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("data packs: " + map);
        for (final Pack info : map.values())
        {
            final PackResources pack = info.open();
            if (pack != null)
            {
                this.allPacks.add(info::open);
                this.folderPacks.add(info::open);
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
