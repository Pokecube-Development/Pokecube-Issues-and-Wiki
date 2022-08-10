package pokecube.api.entity.pokemob;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import pokecube.api.entity.IOngoingAffected;

public class PokemobCaps
{
    public static final Capability<IPokemob> POKEMOB_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IOngoingAffected> AFFECTED_CAP = CapabilityManager.get(new CapabilityToken<>(){});

}
