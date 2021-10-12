package pokecube.core.moves.zmoves;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityZMove
{
    public static class Impl implements ZPower
    {
    }

    private static Impl def = new Impl();

    public static ZPower get(final ICapabilityProvider providerIn)
    {
        return providerIn.getCapability(CapabilityZMove.CAPABILITY).orElse(CapabilityZMove.def);
    }

    public static final Capability<ZPower> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}
