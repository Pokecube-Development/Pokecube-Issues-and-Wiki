package thut.api.attachments;

import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.data.HolderProvider;
import thut.api.entity.IShearable;

public class Shearable
{
    public static class SheepImpl extends Impl
    {
        final Sheep sheep;

        public SheepImpl(final Sheep sheep)
        {
            this.sheep = sheep;
        }

        @Override
        public boolean isSheared()
        {
            return this.sheep.isSheared();
        }

        @Override
        public void shear()
        {
            this.sheep.setSheared(true);
        }
    }

    public static interface IShearableSerializable extends IShearable, INBTSerializable<CompoundTag>
    {}

    public static class WrapperImpl extends Impl
    {
        final IShearable wrapped;

        public WrapperImpl(final IShearable wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean isSheared()
        {
            return this.wrapped.isSheared();
        }

        @Override
        public void shear()
        {
            this.wrapped.shear();
        }

        @Override
        public void shear(final ItemStack shears)
        {
            this.wrapped.shear(shears);
        }
    }

    public static class Impl implements IShearableSerializable
    {
        @Override
        public boolean isSheared()
        {
            return false;
        }

        @Override
        public void shear()
        {}

        @Override
        public @UnknownNullability CompoundTag serializeNBT(Provider provider)
        {
            return null;
        }

        @Override
        public void deserializeNBT(Provider provider, CompoundTag nbt)
        {}
    }

    public static final HolderProvider<IShearableSerializable> _REGISTRY = new HolderProvider<>();

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:shearable");
    public static Supplier<AttachmentType<IShearableSerializable>> TYPE;

    public static IShearable get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE.get())) return in.getData(TYPE.get());
        return null;
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(ID.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
        _REGISTRY.register(new HolderProvider.Provider<IShearableSerializable>()
        {

            @Override
            public IShearableSerializable apply(IAttachmentHolder t)
            {
                if (t instanceof Sheep sheep) return new SheepImpl(sheep);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
    }
}
