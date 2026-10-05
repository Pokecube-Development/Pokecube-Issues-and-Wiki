package thut.api.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import thut.api.data.HolderProvider;

import java.util.List;
import java.util.function.Supplier;

public class Shearable
{
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
        public boolean isShearable(@Nullable Player player, ItemStack item, Level level, BlockPos pos)
        {
            return wrapped.isShearable(player, item, level, pos);
        }

        @Override
        public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos)
        {
            return wrapped.onSheared(player, item, level, pos);
        }

        @Override
        public void spawnShearedDrop(Level level, BlockPos pos, ItemStack drop)
        {
            wrapped.spawnShearedDrop(level, pos, drop);
        }
    }

    public static class Impl implements IShearableSerializable
    {
        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            return null;
        }

        @Override
        public void deserializeNBT(Provider provider, CompoundTag nbt)
        {}
    }

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:shearable");
    public static final HolderProvider<IShearableSerializable> _REGISTRY = new HolderProvider<>(ID);
    public static Supplier<AttachmentType<IShearableSerializable>> TYPE;

    public static IShearable get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE.get())) return in.getData(TYPE.get());
        return null;
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(ID.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
        _REGISTRY.register(new HolderProvider.Provider<>()
        {

            @Override
            public IShearableSerializable apply(IAttachmentHolder t)
            {
                if (t instanceof IShearable sheep) return new WrapperImpl(sheep);
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
