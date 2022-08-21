package pokecube.api.entity.trainers;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

public class TrainerCaps
{

    public static final Capability<IHasNPCAIStates> AISTATES_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static final Capability<IHasPokemobs> HASPOKEMOBS_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static final Capability<IHasMessages> MESSAGES_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static final Capability<IHasRewards> REWARDS_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static final Capability<IHasTrades> TRADES_CAP = CapabilityManager.get(new CapabilityToken<>()
    {
    });

    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        event.register(IHasNPCAIStates.class);
        event.register(IHasPokemobs.class);
        event.register(IHasMessages.class);
        event.register(IHasRewards.class);
        event.register(IHasTrades.class);
    }

    public static IHasPokemobs getHasPokemobs(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasPokemobs holder = entityIn.getCapability(TrainerCaps.HASPOKEMOBS_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasPokemobs cap) return cap;
        return holder;
    }

    public static IHasRewards getHasRewards(final ICapabilityProvider entityIn)
    {
        IHasRewards holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.REWARDS_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasRewards cap) return cap;
        return holder;
    }

    public static IHasTrades getHasTrades(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasTrades holder = entityIn.getCapability(TrainerCaps.TRADES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasTrades cap) return cap;
        return holder;
    }

    public static IHasMessages getMessages(final ICapabilityProvider entityIn)
    {
        IHasMessages holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.MESSAGES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasMessages cap) return cap;
        return holder;
    }

    public static IHasNPCAIStates getNPCAIStates(final ICapabilityProvider entityIn)
    {
        IHasNPCAIStates holder = null;
        if (entityIn == null) return null;
        holder = entityIn.getCapability(TrainerCaps.AISTATES_CAP, null).orElse(null);
        if (holder == null && entityIn instanceof IHasNPCAIStates cap) return cap;
        return holder;
    }
}
