package pokecube.adventures.capabilities;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import thut.api.data.HolderProvider;

public class CapabilityNPCAIStates
{
    public static class DefaultAIStates implements IHasNPCAIStates
    {
        int   state = 0;
        float direction;

        public DefaultAIStates()
        {
            for (final AIState state : AIState.values())
                this.setAIState(state, state.getDefault());
        }

        @Override
        public void deserializeNBT(Provider provider, final CompoundTag nbt)
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
        public CompoundTag serializeNBT(Provider provider)
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
    
    private static final HolderProvider<IHasNPCAIStates> _REGISTRY = new HolderProvider<>();

    public static void registerProvider(HolderProvider.Provider<IHasNPCAIStates> reg)
    {
        _REGISTRY.register(reg);
    }

    public static IHasNPCAIStates make(IAttachmentHolder holder)
    {
        return _REGISTRY.make(holder);
    }
}
