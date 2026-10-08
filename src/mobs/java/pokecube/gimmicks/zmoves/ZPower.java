package pokecube.gimmicks.zmoves;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.Tracker;
import thut.api.data.HolderProvider;
import thut.api.data.HolderProvider.Provider;

public interface ZPower
{
    static List<ZPower> CHECK_ORDER = new ArrayList<>();

    static void addZPower(ZPower type)
    {
        CHECK_ORDER.add(type);
        CHECK_ORDER.sort(Comparator.comparingInt(ZPower::priority));
    }

    default boolean canZMove(final IPokemob pokemob, final String moveIn)
    {
        return CHECK_ORDER.stream().anyMatch(z -> !onZCooldown(pokemob) && z.canZMove(pokemob, moveIn));
    }

    default boolean _onZCooldown(IPokemob pokemob)
    {
        return CHECK_ORDER.stream().anyMatch(z -> z.onZCooldown(pokemob));
    }

    default boolean onZCooldown(final IPokemob pokemob)
    {
        LivingEntity owner = pokemob.getOwner();
        if (owner != null)
        {
            long lastUse = owner.getPersistentData().getLong("pokecube:used-z-move");
            long tick = Tracker.instance().getTick();
            return lastUse + GZMoveManager.Z_MOVE_COOLDOWN > tick;
        }
        return false;
    }

    default int priority()
    {
        return 100;
    }

    public static ZPower get(final IAttachmentHolder in)
    {
        return in.getData(Defaults.TYPE);
    }

    public static class Defaults implements ZPower
    {

        public static final ResourceLocation ID = ResourceLocation.parse("pokecube:z_power");
        public static final HolderProvider<ZPower> _REGISTRY = new HolderProvider<>(ID);
        public static Supplier<AttachmentType<ZPower>> TYPE;

        public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
        {
            TYPE = registry.register(ID.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
            _REGISTRY.register(new Provider<>()
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
