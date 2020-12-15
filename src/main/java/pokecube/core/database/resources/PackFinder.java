package pokecube.core.database.resources;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IPackNameDecorator;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackInfo.IFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.packs.ModFileResourcePack;
import pokecube.core.PokecubeCore;

public class PackFinder implements IPackFinder
{

    private Map<ModFile, IResourcePack> modResourcePacks = Maps.newHashMap();
    public final List<IResourcePack>    allPacks         = Lists.newArrayList();
    public Set<IResourcePack>           folderPacks      = Sets.newHashSet();

    private final FolderPackFinder folderFinder_old;
    private final FolderPackFinder folderFinder_new;

    public PackFinder(final ResourcePackInfo.IFactory packInfoFactoryIn)
    {
        File folder = FMLPaths.GAMEDIR.get().resolve("resourcepacks").toFile();
        folder.mkdirs();
        PokecubeCore.LOGGER.debug("Adding data folder: {}", folder);
        this.folderFinder_old = new FolderPackFinder(folder, IPackNameDecorator.PLAIN);
        folder = FMLPaths.CONFIGDIR.get().resolve(PokecubeCore.MODID).resolve("datapacks").toFile();
        folder.mkdirs();
        PokecubeCore.LOGGER.debug("Adding data folder: {}", folder);
        this.folderFinder_new = new FolderPackFinder(folder, IPackNameDecorator.PLAIN);
        this.init(packInfoFactoryIn);
    }

    public void init(final ResourcePackInfo.IFactory packInfoFactoryIn)
    {
        try
        {
            this.modResourcePacks = ModList.get().getModFiles().stream().map(mf -> new ModFileResourcePack(mf
                    .getFile())).collect(Collectors.toMap(ModFileResourcePack::getModFile, Function.identity()));
            this.allPacks.addAll(this.modResourcePacks.values());
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }

        final Map<String, ResourcePackInfo> map = Maps.newHashMap();
        try
        {
            this.folderFinder_old.findPacks(a -> map.put(a.getName(), a), packInfoFactoryIn);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error checking resourcepacks for data!", e);
        }
        try
        {
            this.folderFinder_new.findPacks(a -> map.put(a.getName(), a), packInfoFactoryIn);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error checking config/pokecube/datapacks for data!", e);
        }

        for (final ResourcePackInfo info : map.values())
        {
            final IResourcePack pack = info.getResourcePack();
            if (pack != null)
            {
                this.allPacks.add(pack);
                this.folderPacks.add(pack);
            }
            else PokecubeCore.LOGGER.debug("No Pack found for " + info);
        }
    }

    @Override
    public void findPacks(final Consumer<ResourcePackInfo> infoConsumer, final IFactory infoFactory)
    {
        throw new RuntimeException("Opps we, don't do this yet!");
    }

}
