package pokecube.adventures.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasTrades.IHasTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;

public class TrainerCaps
{

    @CapabilityInject(IHasNPCAIStates.class)
    public static final Capability<IHasNPCAIStates> AISTATES_CAP = null;

    @CapabilityInject(IHasPokemobs.class)
    public static final Capability<IHasPokemobs> HASPOKEMOBS_CAP = null;

    @CapabilityInject(IHasMessages.class)
    public static final Capability<IHasMessages> MESSAGES_CAP = null;

    @CapabilityInject(IHasRewards.class)
    public static final Capability<IHasRewards> REWARDS_CAP = null;

    @CapabilityInject(IHasTrades.class)
    public static final Capability<IHasTrades> TRADES_CAP = null;

    public static IHasPokemobs getHasPokemobs(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasPokemobs holder = entityIn.getCapability(TrainerCaps.HASPOKEMOBS_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasPokemobs) return (IHasPokemobs) entityIn;
        return holder;
    }

    public static IHasRewards getHasRewards(final ICapabilityProvider entityIn)
    {
        IHasRewards holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.REWARDS_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasRewards) return (IHasRewards) entityIn;
        return holder;
    }

    public static IHasTrades getHasTrades(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasTrades holder = entityIn.getCapability(TrainerCaps.TRADES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasTrades) return (IHasTrades) entityIn;
        return holder;
    }

    public static IHasMessages getMessages(final ICapabilityProvider entityIn)
    {
        IHasMessages holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.MESSAGES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasMessages) return (IHasMessages) entityIn;
        return holder;
    }

    public static IHasNPCAIStates getNPCAIStates(final ICapabilityProvider entityIn)
    {
        IHasNPCAIStates holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.AISTATES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasNPCAIStates) return (IHasNPCAIStates) entityIn;
        return holder;
    }
}
