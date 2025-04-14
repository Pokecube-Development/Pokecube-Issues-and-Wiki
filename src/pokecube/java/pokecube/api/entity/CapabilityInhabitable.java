package pokecube.api.entity;

import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.blocks.IInhabitable.IHabitat;

import java.util.Map;
import java.util.function.Function;

public class CapabilityInhabitable
{
    public static class HabitatProvider implements IHabitat
    {
        // These are public so that they can potentially be replaced as needed.
        // Example: Beaver dams would need a beaver habitat, ant nests need an
        // ant one, but we don't want to have to add multiple instances of the
        // capability, etc.
        private IInhabitable wrapped;

        final BlockEntity tile;

        public HabitatProvider(final BlockEntity tile, final IInhabitable toWrap)
        {
            this.tile = tile;
            this.setWrapped(toWrap);
        }

        public HabitatProvider(final BlockEntity tile)
        {
            this(tile, new NotHabitat());
        }

        @Override
        public void onExitHabitat(final Mob mob)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            this.getWrapped().onExitHabitat(mob);
            mob.setPersistenceRequired();
        }

        @Override
        public boolean onEnterHabitat(final Mob mob)
        {
            if (this.tile.getBlockPos() != null) this.wrapped.setPos(this.tile.getBlockPos());
            mob.setPersistenceRequired();
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

        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            final CompoundTag nbt = new CompoundTag();
            if (this.getWrapped() instanceof INBTSerializable<?> ser)
                nbt.put(this.getWrapped().getKey().toString(), ser.serializeNBT(provider));
            return nbt;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void deserializeNBT(Provider provider, final CompoundTag nbt)
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
                            keyLoc = ResourceLocation.parse(s);
                            key = s;
                            break;
                        }
                        catch (final Exception e)
                        {
                        }
                    if (CapabilityInhabitable.REGISTRY.containsKey(keyLoc))
                        this.setWrapped(CapabilityInhabitable.REGISTRY.get(keyLoc).apply(this.tile));
                }
                if (this.getWrapped() instanceof INBTSerializable)
                    ((INBTSerializable<Tag>) this.getWrapped()).deserializeNBT(provider, nbt.get(key));
            }
            catch (final Exception e)
            {
            }
        }
    }

    private static final Map<ResourceLocation, Function<BlockEntity, IInhabitable>> REGISTRY = Maps.newHashMap();

    public static void Register(ResourceLocation key, Function<BlockEntity, IInhabitable> factory)
    {
        REGISTRY.put(key, factory);
    }

    /**
     * Blank default implementation
     */
    public static class NotHabitat implements IInhabitable
    {
        @Override
        public void onExitHabitat(final Mob mob)
        {}

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

    public static IHabitat makeProvider(final IAttachmentHolder in)
    {
        if (!(in instanceof BlockEntity tile)) return null;
        return new HabitatProvider(tile);
    }
}
