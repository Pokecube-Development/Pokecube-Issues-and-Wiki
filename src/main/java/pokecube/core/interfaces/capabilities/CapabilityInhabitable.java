package pokecube.core.interfaces.capabilities;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.interfaces.IInhabitable;

public class CapabilityInhabitable
{
    @CapabilityInject(IInhabitable.class)
    public static final Capability<IInhabitable> CAPABILITY = null;

    public static class HabitatProvider implements ICapabilityProvider, IInhabitable
    {
        // These are public so that they can potentially be replaced as needed.
        // Example: Beaver dams would need a beaver habitat, ant nests need an
        // ant one, but we don't want to have to add multiple instances of the
        // capability, etc.
        public IInhabitable wrapped;

        public final LazyOptional<IInhabitable> cap_holder;

        public HabitatProvider(final IInhabitable toWrap)
        {
            this.wrapped = toWrap;
            this.cap_holder = LazyOptional.of(() -> this);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityInhabitable.CAPABILITY.orEmpty(cap, this.cap_holder);
        }

        @Override
        public void onExitHabitat(final MobEntity mob)
        {
            this.wrapped.onExitHabitat(mob);
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            return this.wrapped.onEnterHabitat(mob);
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            return this.wrapped.canEnterHabitat(mob);
        }

        @Override
        public void onTick(final BlockPos pos, final ServerWorld world)
        {
            this.wrapped.onTick(pos, world);
        }

        @Override
        public void onBroken(final BlockPos pos, final ServerWorld world)
        {
            this.wrapped.onBroken(pos, world);
        }
    }

    private static Map<ResourceLocation, Supplier<IInhabitable>> REGISTRY = Maps.newHashMap();

    public static void Register(final ResourceLocation key, final Supplier<IInhabitable> factory)
    {
        CapabilityInhabitable.REGISTRY.put(key, factory);
    }

    public static IInhabitable make(final ResourceLocation key)
    {
        return CapabilityInhabitable.REGISTRY.getOrDefault(key, () -> null).get();
    }

    public static class SaveableHabitatProvider extends HabitatProvider implements ICapabilitySerializable<CompoundNBT>
    {

        public SaveableHabitatProvider(final IInhabitable toWrap)
        {
            super(toWrap);
        }

        public SaveableHabitatProvider()
        {
            this(new NotHabitat());
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            if (this.wrapped instanceof INBTSerializable) nbt.put(this.wrapped.getKey().toString(),
                    ((INBTSerializable<?>) this.wrapped).serializeNBT());
            return nbt;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            try
            {
                String key = this.wrapped.getKey() == null ? null : this.wrapped.getKey().toString();

                if (key == null || !nbt.contains(key))
                {
                    ResourceLocation keyLoc = null;
                    for (final String s : nbt.keySet())
                        try
                        {
                            keyLoc = new ResourceLocation(s);
                            key = s;
                            break;
                        }
                        catch (final Exception e)
                        {
                        }
                    if (CapabilityInhabitable.REGISTRY.containsKey(keyLoc))
                        this.wrapped = CapabilityInhabitable.REGISTRY.get(keyLoc).get();
                }
                if (this.wrapped instanceof INBTSerializable) ((INBTSerializable<INBT>) this.wrapped).deserializeNBT(nbt
                        .get(key));
            }
            catch (final Exception e)
            {
            }
        }

    }

    /**
     * Blank default implementation
     */
    public static class NotHabitat implements IInhabitable
    {
        @Override
        public void onExitHabitat(final MobEntity mob)
        {
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            return false;
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            return false;
        }
    }

    public static class Storage implements Capability.IStorage<IInhabitable>
    {

        @SuppressWarnings({ "unchecked" })
        @Override
        public void readNBT(final Capability<IInhabitable> capability, final IInhabitable instance,
                final Direction side, final INBT nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable<INBT>) instance).deserializeNBT(
                    nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IInhabitable> capability, final IInhabitable instance,
                final Direction side)
        {
            if (instance instanceof ICapabilitySerializable) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

}
