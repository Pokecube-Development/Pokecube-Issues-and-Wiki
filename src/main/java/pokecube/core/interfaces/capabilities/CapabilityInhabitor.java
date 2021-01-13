package pokecube.core.interfaces.capabilities;

import java.util.Optional;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.ai.tasks.idle.bees.BeeTasks;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IPokemob;

public class CapabilityInhabitor
{
    @CapabilityInject(IInhabitor.class)
    public static final Capability<IInhabitor> CAPABILITY = null;

    public static class InhabitorProvider implements ICapabilityProvider
    {
        protected final IInhabitor wrapped;

        private final LazyOptional<IInhabitor> cap_holder;

        public InhabitorProvider(final IInhabitor toWrap)
        {
            this.wrapped = toWrap;
            this.cap_holder = LazyOptional.of(() -> this.wrapped);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityInhabitor.CAPABILITY.orEmpty(cap, this.cap_holder);
        }
    }

    public static class NotInhabitor implements IInhabitor
    {

        @Override
        public GlobalPos getHome()
        {
            return null;
        }

        @Override
        public void onExitHabitat()
        {
        }

        @Override
        public GlobalPos getWorkSite()
        {
            return null;
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
        }
    }

    public static class BeeInhabitor implements IInhabitor
    {
        final MobEntity bee;

        public BeeInhabitor(final MobEntity bee)
        {
            this.bee = bee;
        }

        @Override
        public GlobalPos getHome()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.HIVE_POS)) return null;
            return brain.getMemory(BeeTasks.HIVE_POS).get();
        }

        @Override
        public void onExitHabitat()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.HAS_NECTAR)) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(this.bee);
            if (pokemob != null && nectar) pokemob.eat(ItemStack.EMPTY);
            brain.removeMemory(BeeTasks.HAS_NECTAR);
        }

        @Override
        public GlobalPos getWorkSite()
        {
            final Brain<?> brain = this.bee.getBrain();
            if (!brain.hasMemory(BeeTasks.FLOWER_POS)) return null;
            return brain.getMemory(BeeTasks.FLOWER_POS).get();
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
            final Brain<?> brain = this.bee.getBrain();
            if (site == null) brain.removeMemory(BeeTasks.FLOWER_POS);
            else brain.setMemory(BeeTasks.FLOWER_POS, site);
        }

    }

    public static class Storage implements Capability.IStorage<IInhabitor>
    {

        @SuppressWarnings({ "unchecked" })
        @Override
        public void readNBT(final Capability<IInhabitor> capability, final IInhabitor instance,
                final Direction side, final INBT nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable<INBT>) instance).deserializeNBT(
                    nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IInhabitor> capability, final IInhabitor instance,
                final Direction side)
        {
            if (instance instanceof ICapabilitySerializable) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

}
