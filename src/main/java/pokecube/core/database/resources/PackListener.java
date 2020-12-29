package pokecube.core.database.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleReloadableResourceManager;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;

public class PackListener implements IFutureReloadListener
{
    public boolean loaded = false;

    @Override
    public final CompletableFuture<Void> reload(final IFutureReloadListener.IStage stage,
            final IResourceManager resourceManager, final IProfiler preparationsProfiler,
            final IProfiler reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor)
    {
        return CompletableFuture.completedFuture((Void) null).thenCompose(stage::markCompleteAwaitingOthers)
                .thenAcceptAsync((v) ->
                {
                    this.add(resourceManager);
                }, gameExecutor);
    }

    public void add(final IResourceManager resourceManager)
    {
        if (!(resourceManager instanceof SimpleReloadableResourceManager)) return;
        Database.resourceManager = (IReloadableResourceManager) resourceManager;
        // Initialize the resourceloader.
        Database.loadCustomPacks(false);
        for (final IResourcePack pack : Database.customPacks)
        {
            PokecubeCore.LOGGER.debug("Reloading Pack: " + pack.getName());
            ((SimpleReloadableResourceManager) resourceManager).addResourcePack(pack);
        }
        this.loaded = true;
    }
}
