package pokecube.core.utils;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.items.megastuff.IMegaCapability;

public class CapHolders
{
    public static final Capability<IGuardAICapability> GUARDAI_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IMegaCapability>    MEGA_CAP    = CapabilityManager.get(new CapabilityToken<>(){});
}
