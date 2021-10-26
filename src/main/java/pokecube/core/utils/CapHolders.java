package pokecube.core.utils;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.items.megastuff.IMegaCapability;

public class CapHolders
{
    @CapabilityInject(IGuardAICapability.class)
    public static final Capability<IGuardAICapability> GUARDAI_CAP = null;
    @CapabilityInject(IMegaCapability.class)
    public static final Capability<IMegaCapability>    MEGA_CAP    = null;
}
