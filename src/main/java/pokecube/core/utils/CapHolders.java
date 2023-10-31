package pokecube.core.utils;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.api.ai.IInhabitor;
import pokecube.api.blocks.IInhabitable;
import pokecube.core.ai.routes.IGuardAICapability;

public class CapHolders
{
    public static final Capability<IInhabitor> IIHABITOR_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IInhabitable> IIHABITABLE_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IGuardAICapability> GUARDAI_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    
    public static IInhabitor getInhabitor(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(CapHolders.IIHABITOR_CAP).orElse(null);
    }
    public static IInhabitable getInhabitable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(CapHolders.IIHABITABLE_CAP).orElse(null);
    }
    public static IGuardAICapability getGuardAI(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
    }
}
