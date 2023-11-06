package pokecube.api.entity.pokemob;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.items.IPokemobUseable;
import pokecube.core.utils.EntityTools;

public class PokemobCaps
{
    public static final Capability<IPokemob> POKEMOB_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IOngoingAffected> AFFECTED_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<IPokemobUseable> USABLEITEM_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static IPokemobUseable getPokemobUsable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(USABLEITEM_CAP).orElse(null);
    }

    public static IPokemob getPokemobFor(ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        if (entityIn instanceof Entity entity) entityIn = EntityTools.getCoreEntity(entity);
        final IPokemob pokemobHolder = entityIn.getCapability(PokemobCaps.POKEMOB_CAP, null).orElse(null);
        if (pokemobHolder == null && IPokemob.class.isInstance(entityIn)) return IPokemob.class.cast(entityIn);
        return pokemobHolder;
    }

    public static IOngoingAffected getAffected(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IOngoingAffected var = entityIn.getCapability(PokemobCaps.AFFECTED_CAP, null).orElse(null);
        if (var != null) return var;
        else if (IOngoingAffected.class.isInstance(entityIn)) return IOngoingAffected.class.cast(entityIn);
        return null;
    }
}
