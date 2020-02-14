package pokecube.core.database.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import pokecube.core.database.Database;

public class PackListener implements IFutureReloadListener
{
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
        for (final IResourcePack pack : Database.customPacks)
            resourceManager.addResourcePack(pack);
    }
}
