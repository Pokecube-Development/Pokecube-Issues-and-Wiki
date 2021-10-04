package pokecube.core.interfaces.capabilities;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        private IInhabitable wrapped;

        final BlockEntity tile;

        public final LazyOptional<IInhabitable> cap_holder;

        public HabitatProvider(final BlockEntity tile, final IInhabitable toWrap)
        {
            this.tile = tile;
            this.setWrapped(toWrap);
            this.cap_holder = LazyOptional.of(() -> this);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityInhabitable.CAPABILITY.orEmpty(cap, this.cap_holder);
        }

        @Override
        public void onExitHabitat(final Mob mob)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            this.getWrapped().onExitHabitat(mob);
        }

        @Override
        public boolean onEnterHabitat(final Mob mob)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            return this.getWrapped().onEnterHabitat(mob);
        }

        @Override
        public boolean canEnterHabitat(final Mob mob)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            return this.getWrapped().canEnterHabitat(mob);
        }

        @Override
        public void onTick(final ServerLevel world)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            this.getWrapped().onTick(world);
        }

        @Override
        public void onBroken(final ServerLevel world)
        {
            this.getWrapped().onBroken(world);
        }

        public IInhabitable getWrapped()
        {
            return this.wrapped;
        }

        public void setWrapped(final IInhabitable wrapped)
        {
            this.wrapped = wrapped;
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

    public static class SaveableHabitatProvider extends HabitatProvider implements ICapabilitySerializable<CompoundTag>
    {

        public SaveableHabitatProvider(final BlockEntity tile)
        {
            super(tile, new NotHabitat());
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            if (this.getWrapped() instanceof INBTSerializable) nbt.put(this.getWrapped().getKey().toString(),
                    ((INBTSerializable<?>) this.getWrapped()).serializeNBT());
            return nbt;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            try
            {
                String key = this.getWrapped().getKey() == null ? null : this.getWrapped().getKey().toString();

                if (key == null || !nbt.contains(key))
                {
                    ResourceLocation keyLoc = null;
                    for (final String s : nbt.getAllKeys())
                        try
                        {
                            keyLoc = new ResourceLocation(s);
                            key = s;
                            break;
                        }
                        catch (final Exception e)
                        {
                        }
                    if (CapabilityInhabitable.REGISTRY.containsKey(keyLoc)) this.setWrapped(
                            CapabilityInhabitable.REGISTRY.get(keyLoc).get());
                }
                if (this.getWrapped() instanceof INBTSerializable) ((INBTSerializable<Tag>) this.getWrapped())
                        .deserializeNBT(nbt.get(key));
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
        public void onExitHabitat(final Mob mob)
        {
        }

        @Override
        public boolean onEnterHabitat(final Mob mob)
        {
            return false;
        }

        @Override
        public boolean canEnterHabitat(final Mob mob)
        {
            return false;
        }
    }

    public static class Storage implements Capability.IStorage<IInhabitable>
    {

        @SuppressWarnings({ "unchecked" })
        @Override
        public void readNBT(final Capability<IInhabitable> capability, final IInhabitable instance,
                final Direction side, final Tag nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable<Tag>) instance).deserializeNBT(
                    nbt);
        }

        @Override
        public Tag writeNBT(final Capability<IInhabitable> capability, final IInhabitable instance,
                final Direction side)
        {
            if (instance instanceof ICapabilitySerializable) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

}
