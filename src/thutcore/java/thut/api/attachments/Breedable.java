package thut.api.attachments;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.data.HolderProvider;
import thut.api.entity.IBreedingMob;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class Breedable
{

    public static class AgeableWrapper implements IBreedingMob
    {
        final AgeableMob wrapped;

        public AgeableWrapper(final AgeableMob wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public Object getChild(final IBreedingMob male)
        {
            var maleMob = male.getEntity();
            if(!(maleMob instanceof AgeableMob aged)) return null;
            return this.wrapped.getBreedOffspring((ServerLevel) this.wrapped.level(), aged);
        }

        @Override
        public boolean canMate(final AgeableMob other)
        {
            try
            {
                if (this.wrapped instanceof Animal a && other instanceof Animal b) return a.canMate(b);
            }
            catch (final Exception e)
            {
                if (!ThutCore.conf.supress_warns)
                    ThutCore.LOGGER.warn("Warning, Mob {} has messed up canMateWith check!",
                            RegHelper.getKey(this.wrapped.getType()));
                return false;
            }
            return other.getClass() == this.wrapped.getClass();
        }

        @Override
        public boolean canBreed()
        {
            if (this.wrapped instanceof Animal animal) return animal.canBreed();
            return false;
        }

        @Override
        public boolean isBreeding()
        {
            if (this.wrapped instanceof Animal animal) return animal.isInLove();
            return false;
        }

        @Override
        public void setReadyToMate(@Nullable final Player cause)
        {
            if (this.wrapped instanceof Animal animal) animal.setInLove(cause);
        }

        @Override
        public void resetLoveStatus()
        {
            if (this.wrapped instanceof Animal animal) animal.resetLove();
        }

        @Override
        public ServerPlayer getCause()
        {
            if (this.wrapped instanceof Animal animal) return animal.getLoveCause();
            return null;
        }

        @Override
        public Mob getEntity()
        {
            return wrapped;
        }
    }

    public static IBreedingMob get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE.get())) return in.getData(TYPE.get());
        var resp = _REGISTRY.make(in);
        return resp==null?resp:in.getData(TYPE.get());
    }

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:breedable");
    public static final HolderProvider<IBreedingMob> _REGISTRY = new HolderProvider<>(ID);
    public static Supplier<AttachmentType<IBreedingMob>> TYPE;

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(ID.getPath(), () -> AttachmentType.builder(_REGISTRY::make).build());
        _REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            public IBreedingMob apply(IAttachmentHolder t)
            {
                if (t instanceof AgeableMob living) return new AgeableWrapper(living);
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
