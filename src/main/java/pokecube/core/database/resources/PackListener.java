package pokecube.core.database.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;

public class PackListener implements PreparableReloadListener
{
    public boolean loaded = false;

    @Override
    public final CompletableFuture<Void> reload(final PreparableReloadListener.PreparationBarrier stage,
            final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler,
            final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor)
    {
        return CompletableFuture.completedFuture((Void) null).thenCompose(stage::wait)
                .thenAcceptAsync((v) ->
                {
                    this.add(resourceManager);
                }, gameExecutor);
    }

    public void add(final ResourceManager resourceManager)
    {
        if (!(resourceManager instanceof SimpleReloadableResourceManager)) return;
        Database.resourceManager = (ReloadableResourceManager) resourceManager;
        // Initialize the resourceloader.
        Database.loadCustomPacks(false);
        for (final PackResources pack : Database.customPacks)
        {
            PokecubeCore.LOGGER.debug("Reloading Pack: " + pack.getName());
            ((SimpleReloadableResourceManager) resourceManager).add(pack);
        }
        this.loaded = true;
    }
}
