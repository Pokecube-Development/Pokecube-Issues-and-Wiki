package pokecube.adventures.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.TrainerCaps;

public class CapabilityNPCAIStates
{
    public static class DefaultAIStates implements IHasNPCAIStates, ICapabilityProvider
    {
        int   state = 0;
        float direction;

        private final LazyOptional<IHasNPCAIStates> holder = LazyOptional.of(() -> this);

        public DefaultAIStates()
        {
            for (final AIState state : AIState.values())
                this.setAIState(state, state.getDefault());
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.setTotalState(nbt.getInt("AI"));
            this.setDirection(nbt.getFloat("D"));
        }

        @Override
        public boolean getAIState(final AIState state)
        {
            // These two have config overrides, which ignore the actual ai
            // states.
            if (state == AIState.TRADES_ITEMS && !PokecubeAdv.config.trainersTradeItems) return false;
            if (state == AIState.TRADES_MOBS && !PokecubeAdv.config.trainersTradeMobs) return false;

            return (this.state & state.getMask()) > 0;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TrainerCaps.AISTATES_CAP.orEmpty(cap, this.holder);
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
        public CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("AI", this.getTotalState());
            tag.putFloat("D", this.getDirection());
            return tag;
        }

        @Override
        public void setAIState(final AIState state, final boolean flag)
        {
            if (flag) this.state = Integer.valueOf(this.state | state.getMask());
            else this.state = Integer.valueOf(this.state & -state.getMask() - 1);
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
}
