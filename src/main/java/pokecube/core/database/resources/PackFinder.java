package pokecube.core.database.resources;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.FolderPackFinder;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
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

    private final FolderPackFinder folderFinder;

    public PackFinder(final ResourcePackInfo.IFactory<ResourcePackInfo> packInfoFactoryIn)
    {
        final File folder = new File(FMLPaths.GAMEDIR.get().toFile(), "resourcepacks");
        PokecubeCore.LOGGER.debug("Setting resourcepacks folder as: {}", folder);
        this.folderFinder = new FolderPackFinder(folder);
        this.init(packInfoFactoryIn);
    }

    public void init(final ResourcePackInfo.IFactory<ResourcePackInfo> packInfoFactoryIn)
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
            this.folderFinder.addPackInfosToMap(map, packInfoFactoryIn);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.fatal("Error checking resourcepacks for data!", e);
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
    public <T extends ResourcePackInfo> void addPackInfosToMap(final Map<String, T> packList,
            final ResourcePackInfo.IFactory<T> factory)
    {
        throw new RuntimeException("Opps we, don't do this yet!");
    }

}
