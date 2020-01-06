package pokecube.adventures.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityNPCAIStates
{
    public static class DefaultAIStates implements IHasNPCAIStates, ICapabilitySerializable<INBT>
    {
        int                                         state  = 0;
        float                                       direction;
        private final LazyOptional<IHasNPCAIStates> holder = LazyOptional.of(() -> this);

        public DefaultAIStates()
        {
        }

        @Override
        public void deserializeNBT(final INBT nbt)
        {
            CapabilityNPCAIStates.storage.readNBT(CapabilityNPCAIStates.AISTATES_CAP, this, null, nbt);
        }

        @Override
        public boolean getAIState(final int state)
        {
            return (this.state & state) > 0;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityNPCAIStates.AISTATES_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public float getDirection()
        {
            return this.direction;
        }

        @Override
        public int getTotalState()
        {
            return this.state;
        }

        @Override
        public INBT serializeNBT()
        {
            return CapabilityNPCAIStates.storage.writeNBT(CapabilityNPCAIStates.AISTATES_CAP, this, null);
        }

        @Override
        public void setAIState(final int state, final boolean flag)
        {
            if (flag) this.state = Integer.valueOf(this.state | state);
            else this.state = Integer.valueOf(this.state & -state - 1);
        }

        @Override
        public void setDirection(final float direction)
        {
            this.direction = direction;
        }

        @Override
        public void setTotalState(final int state)
        {
            this.state = state;
        }

    }

    public static interface IHasNPCAIStates
    {
        public static final int STATIONARY     = 1 << 0;
        public static final int INBATTLE       = 1 << 1;
        public static final int THROWING       = 1 << 2;
        public static final int PERMFRIENDLY   = 1 << 3;
        public static final int FIXEDDIRECTION = 1 << 4;
        public static final int MATES          = 1 << 5;
        public static final int INVULNERABLE   = 1 << 6;
        public static final int TRADES         = 1 << 7;

        boolean getAIState(int state);

        /** @return Direction to face if FIXEDDIRECTION */
        public float getDirection();

        int getTotalState();

        void setAIState(int state, boolean flag);

        /**
         * @param direction
         *            Direction to face if FIXEDDIRECTION
         */
        public void setDirection(float direction);

        void setTotalState(int state);
    }

    public static class Storage implements Capability.IStorage<IHasNPCAIStates>
    {

        @Override
        public void readNBT(final Capability<IHasNPCAIStates> capability, final IHasNPCAIStates instance,
                final Direction side, final INBT nbt)
        {
            if (nbt instanceof IntNBT) instance.setTotalState(((IntNBT) nbt).getInt());
            else if (nbt instanceof CompoundNBT)
            {
                final CompoundNBT tag = (CompoundNBT) nbt;
                instance.setTotalState(tag.getInt("AI"));
                instance.setDirection(tag.getFloat("D"));
            }
        }

        @Override
        public INBT writeNBT(final Capability<IHasNPCAIStates> capability, final IHasNPCAIStates instance,
                final Direction side)
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putInt("AI", instance.getTotalState());
            tag.putFloat("D", instance.getDirection());
            return tag;
        }

    }

    @CapabilityInject(IHasNPCAIStates.class)
    public static final Capability<IHasNPCAIStates> AISTATES_CAP = null;

    public static Storage storage;

    public static IHasNPCAIStates getNPCAIStates(final ICapabilityProvider entityIn)
    {
        IHasNPCAIStates holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(CapabilityNPCAIStates.AISTATES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasNPCAIStates) return (IHasNPCAIStates) entityIn;
        return holder;
    }
}
