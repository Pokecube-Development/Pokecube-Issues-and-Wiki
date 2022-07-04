package pokecube.core.database.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;

public class PackListener implements PreparableReloadListener
{
    public boolean loaded = false;

    public static void addPack(PackResources pack, ReloadableResourceManager manager)
    {
        for (String s : pack.getNamespaces(PackType.SERVER_DATA))
        {
            ((MultiPackResourceManager) manager.resources).namespacedManagers.computeIfAbsent(s, (namespace) -> {
                return new FallbackResourceManager(PackType.SERVER_DATA, namespace);
            }).add(pack);
        }
    }

    @Override
    public final CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier stage,
            final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler,
            final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor)
    {
        return CompletableFuture.completedFuture((Void) null).thenCompose(stage::wait).thenAcceptAsync((v) -> {
            this.add(resourceManager);
        }, gameExecutor);
    }

    public void add(final ResourceManager resourceManager)
    {
        if (!(resourceManager instanceof ReloadableResourceManager manager)) return;
        Database.resourceManager = manager;
        // Initialize the resourceloader.
        Database.loadCustomPacks(false);

        for (final PackResources pack : Database.customPacks)
        {
            PokecubeCore.LOGGER.debug("Reloading Pack: " + pack.getName());
            PackListener.addPack(pack, manager);
        }
        this.loaded = true;
    }
}
