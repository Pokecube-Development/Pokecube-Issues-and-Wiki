package thut.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class HolderProvider<T>
{
    private List<Provider<T>> _PROVIDERS = new ArrayList<>();
    private Map<ResourceLocation, Provider<T>> _PROVIDER_REG = new HashMap<>();
    public ResourceLocation key;

    public HolderProvider(ResourceLocation key)
    {
        this.key = key;
    }

    public void register(Provider<T> reg)
    {
        _PROVIDER_REG.put(reg.key(), reg);
        _PROVIDERS.clear();
        _PROVIDERS.addAll(_PROVIDER_REG.values());
        _PROVIDERS.sort(null);
    }

    public T make(IAttachmentHolder holder)
    {
        for (var p : _PROVIDERS)
        {
            var p2 = p.apply(holder);
            if (p2 != null)
            {
                return p2;
            }
        }
        Thread.dumpStack();
        return null;
    }

    public static abstract class Provider<T> implements Function<IAttachmentHolder, T>, Comparable<Provider<T>>
    {
        public int getPriority()
        {
            return 100;
        }

        @Override
        public int compareTo(Provider<T> o)
        {
            return getPriority() - o.getPriority();
        }

        protected abstract ResourceLocation key();
    }
}
