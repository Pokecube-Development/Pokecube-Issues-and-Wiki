package pokecube.mixin.entity;

import java.util.Map;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.utils.EntityTools;

@Mixin(Entity.class)
public abstract class ICapabilityCache extends CapabilityProvider<Entity>
{
    protected ICapabilityCache(Class<Entity> baseClass)
    {
        super(baseClass);
    }

    Map<Object, Object> CAPCACHE = new Object2ObjectOpenHashMap<>();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap)
    {
        if (!EntityTools.isCached(cap))
        {
            ICapabilityProvider us = (this);
            return us.getCapability(cap, null);
        }
        @SuppressWarnings("unchecked")
        LazyOptional<T> value = (LazyOptional<T>) CAPCACHE.computeIfAbsent(cap, c -> {
            ICapabilityProvider us = (this);
            return us.getCapability(cap, null);
        });
        if (!value.isPresent() || !value.resolve().isPresent()) CAPCACHE.remove(cap);
        return value;
    }
}
