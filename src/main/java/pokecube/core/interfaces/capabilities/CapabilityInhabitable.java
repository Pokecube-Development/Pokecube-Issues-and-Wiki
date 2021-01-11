package pokecube.core.interfaces.capabilities;

import java.util.Optional;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.core.ai.tasks.idle.bees.BeeTasks;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;

public class CapabilityInhabitable
{
    @CapabilityInject(IInhabitable.class)
    public static final Capability<IInhabitable> CAPABILITY = null;

    public static class HabitatProvider implements ICapabilityProvider
    {
        protected final IInhabitable wrapped;

        private final LazyOptional<IInhabitable> cap_holder;

        public HabitatProvider(final IInhabitable toWrap)
        {
            this.wrapped = toWrap;
            this.cap_holder = LazyOptional.of(() -> this.wrapped);
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityInhabitable.CAPABILITY.orEmpty(cap, this.cap_holder);
        }
    }

    public static class SaveableHabitatProvider extends HabitatProvider implements ICapabilitySerializable<CompoundNBT>
    {

        public SaveableHabitatProvider(final IInhabitable toWrap)
        {
            super(toWrap);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            nbt.put("data", ((ICapabilitySerializable<?>) this.wrapped).serializeNBT());
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            ((ICapabilitySerializable<INBT>) this.wrapped).deserializeNBT(nbt.get("data"));
        }

    }

    /**
     * Blank default implementation
     */
    public static class NotHabitat implements IInhabitable
    {
        @Override
        public void onLeaveHabitat(final MobEntity mob)
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

    public static class BeeHabitat implements IInhabitable
    {

        final BeehiveTileEntity hive;

        public BeeHabitat(final BeehiveTileEntity tile)
        {
            this.hive = tile;
        }

        @Override
        public void onLeaveHabitat(final MobEntity mob)
        {
            if (!BeeTasks.isValidBee(mob)) return;
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            final Brain<?> brain = pokemob.getEntity().getBrain();
            if (!brain.hasMemory(BeeTasks.HAS_NECTAR)) return;
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            if (nectar)
            {
                brain.removeMemory(BeeTasks.HAS_NECTAR);
                pokemob.eat(ItemStack.EMPTY);
                final World world = mob.getEntityWorld();
                final BlockState state = world.getBlockState(this.hive.getPos());
                if (state.getBlock().isIn(BlockTags.BEEHIVES))
                {
                    final int i = BeehiveTileEntity.getHoneyLevel(state);
                    if (i < 5)
                    {
                        int j = world.rand.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) --j;
                        world.setBlockState(this.hive.getPos(), state.with(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i
                                + j)));
                    }
                }
            }
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            final int num = this.hive.bees.size();
            final Brain<?> brain = mob.getBrain();
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            // Try to enter the hive
            this.hive.tryEnterHive(mob, nectar);
            // If this changed, then we added correctly.
            final boolean added = num < this.hive.bees.size();
            // BeehiveTileEntity checks this boolean directly for if
            // there is nectar in the bee.
            if (added) this.hive.bees.get(num).entityData.putBoolean("HasNectar", nectar);
            return added;
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            if (!BeeTasks.isValidBee(mob)) return false;
            return !this.hive.isFullOfBees();
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
