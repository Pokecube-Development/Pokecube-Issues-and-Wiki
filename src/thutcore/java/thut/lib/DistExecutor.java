package thut.lib;

import java.util.function.Supplier;

import net.neoforged.fml.loading.FMLEnvironment;

public class DistExecutor
{
    public static <T> T runForDist(Supplier<Supplier<T>> client, Supplier<Supplier<T>> both)
    {
        return FMLEnvironment.dist.isClient() ? client.get().get() : both.get().get();
    }
}
