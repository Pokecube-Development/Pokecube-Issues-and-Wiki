package pokecube.gimmicks.zmoves;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.data.HolderProvider;
import thut.api.data.HolderProvider.Provider;

public interface ZPower
{
    default boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        // TODO make the default actually check things...
        return true;
    }

    public static ZPower get(final IAttachmentHolder in)
    {
        return in.getData(Defaults.TYPE.get());
    }

    public static class Defaults implements ZPower
    {

        public static final ResourceLocation ID = ResourceLocation.parse("pokecube:z_power");
        public static final HolderProvider<ZPower> _REGISTRY = new HolderProvider<>(ID);
        public static Supplier<AttachmentType<ZPower>> TYPE;

        public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
        {
            TYPE = registry.register(ID.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
            _REGISTRY.register(new Provider<ZPower>()
            {
                @Override
                public ZPower apply(IAttachmentHolder t)
                {
                    return new Defaults();
                }

                @Override
                protected ResourceLocation key()
                {
                    return ID;
                }
            });
        }
    }
}
