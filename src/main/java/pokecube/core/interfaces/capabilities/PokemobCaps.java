package pokecube.core.interfaces.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;

public class PokemobCaps
{

    @CapabilityInject(IPokemob.class)
    public static final Capability<IPokemob> POKEMOB_CAP = null;

    @CapabilityInject(IOngoingAffected.class)
    public static final Capability<IOngoingAffected> AFFECTED_CAP = null;

}
